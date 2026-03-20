package com.dji.sample.manage.service.impl;

import com.dji.sample.manage.model.dto.*;
import com.dji.sample.manage.model.param.DeviceQueryParam;
import com.dji.sample.manage.service.*;
import com.dji.sdk.cloudapi.device.DeviceDomainEnum;
import com.dji.sdk.cloudapi.device.OsdCamera;
import com.dji.sdk.cloudapi.device.OsdDockDrone;
import com.dji.sdk.cloudapi.device.OsdRcDrone;
import com.dji.sdk.cloudapi.device.RcDronePayload;
import com.dji.sdk.cloudapi.device.VideoId;
import com.dji.sdk.cloudapi.livestream.*;
import com.dji.sdk.cloudapi.livestream.api.AbstractLivestreamService;
import com.dji.sdk.common.HttpResultResponse;
import com.dji.sdk.common.SDKManager;
import com.dji.sdk.mqtt.services.ServicesReplyData;
import com.dji.sdk.mqtt.services.TopicServicesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author sean.zhou
 * @date 2021/11/22
 * @version 0.1
 */
@Service
@Transactional
@Slf4j
public class LiveStreamServiceImpl implements ILiveStreamService {

    @Autowired
    private ICapacityCameraService capacityCameraService;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private IWorkspaceService workspaceService;

    @Autowired
    private IDeviceRedisService deviceRedisService;

    @Autowired
    private AbstractLivestreamService abstractLivestreamService;

    @Override
    public List<CapacityDeviceDTO> getLiveCapacity(String workspaceId) {

        // Query all devices in this workspace.
        List<DeviceDTO> devicesList = deviceService.getDevicesByParams(
                DeviceQueryParam.builder()
                        .workspaceId(workspaceId)
                        .domains(List.of(DeviceDomainEnum.DRONE.getDomain(), DeviceDomainEnum.DOCK.getDomain()))
                        .build());

        // Query the live capability of each drone.
        return devicesList.stream()
                .filter(device -> deviceRedisService.checkDeviceOnline(device.getDeviceSn()))
            .map(device -> {
                List<CapacityCameraDTO> cameras = resolveCapacityCamera(device.getDeviceSn());
                log.debug("live capacity resolved. sn={}, cameras={}", device.getDeviceSn(), cameras == null ? 0 : cameras.size());
                return CapacityDeviceDTO.builder()
                    .name(Objects.requireNonNullElse(device.getNickname(), device.getDeviceName()))
                    .sn(device.getDeviceSn())
                    .camerasList(cameras)
                    .build();
            })
                .collect(Collectors.toList());
    }

    private List<CapacityCameraDTO> resolveCapacityCamera(String deviceSn) {
        List<CapacityCameraDTO> fromLiveCapacity = capacityCameraService.getCapacityCameraByDeviceSn(deviceSn);
        if (fromLiveCapacity != null && !fromLiveCapacity.isEmpty()) {
            return normalizeCapacityCameraList(fromLiveCapacity);
        }

        Optional<OsdRcDrone> rcDroneOsdOpt = deviceRedisService.getDeviceOsd(deviceSn, OsdRcDrone.class);
        if (rcDroneOsdOpt.isPresent() && rcDroneOsdOpt.get().getPayloads() != null) {
            return rcDroneOsdOpt.get().getPayloads().stream()
                    .filter(Objects::nonNull)
                    .map(RcDronePayload::getPayloadIndex)
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(this::buildFallbackCamera)
                    .collect(Collectors.toList());
        }

        Optional<OsdDockDrone> dockDroneOsdOpt = deviceRedisService.getDeviceOsd(deviceSn, OsdDockDrone.class);
        if (dockDroneOsdOpt.isPresent() && dockDroneOsdOpt.get().getCameras() != null) {
            return toCapacityCameraList(dockDroneOsdOpt.get().getCameras());
        }

        return normalizeCapacityCameraList(fromLiveCapacity);
    }

    private List<CapacityCameraDTO> normalizeCapacityCameraList(List<CapacityCameraDTO> cameras) {
        if (cameras == null) {
            return List.of();
        }

        return cameras.stream()
                .filter(Objects::nonNull)
                .peek(camera -> {
                    if (camera.getName() == null || camera.getName().isBlank()) {
                        camera.setName(camera.getIndex());
                    }
                })
                .collect(Collectors.toList());
    }

    private List<CapacityCameraDTO> toCapacityCameraList(List<OsdCamera> osdCameras) {
        return osdCameras.stream()
                .filter(Objects::nonNull)
                .map(this::toCapacityCamera)
                .collect(Collectors.toList());
    }

    private CapacityCameraDTO toCapacityCamera(OsdCamera osdCamera) {
        String index = Optional.ofNullable(osdCamera.getPayloadIndex())
                .map(Object::toString)
                .orElse("");

        return buildFallbackCamera(index);
        }

        private CapacityCameraDTO buildFallbackCamera(String index) {

        return CapacityCameraDTO.builder()
                .name(index)
                .index(index)
                .type("normal")
                .videosList(List.of(CapacityVideoDTO.builder()
                        .index("normal-0")
                        .type("normal")
                        .switchVideoTypes(List.of("wide", "zoom", "ir"))
                        .build()))
                .build();
    }

    @Override
    public HttpResultResponse liveStart(LiveTypeDTO liveParam) {
        // Check if this lens is available live.
        HttpResultResponse<DeviceDTO> responseResult = this.checkBeforeLive(liveParam.getVideoId());
        if (HttpResultResponse.CODE_SUCCESS != responseResult.getCode()) {
            return responseResult;
        }

        ILivestreamUrl url = LiveStreamProperty.get(liveParam.getUrlType());
        url = setExt(liveParam.getUrlType(), url, liveParam.getVideoId());

        TopicServicesResponse<ServicesReplyData<Object>> response = abstractLivestreamService.liveStartPush(
                SDKManager.getDeviceSDK(responseResult.getData().getDeviceSn()),
                new LiveStartPushRequest()
                        .setUrl(url)
                        .setUrlType(liveParam.getUrlType())
                        .setVideoId(liveParam.getVideoId())
                        .setVideoQuality(liveParam.getVideoQuality()));

        if (!response.getData().getResult().isSuccess()) {
            return HttpResultResponse.error(response.getData().getResult());
        }

        LiveDTO live = new LiveDTO();

        switch (liveParam.getUrlType()) {
            case AGORA:
                break;
            case RTMP:
                live.setUrl(url.toString().replace("rtmp", "webrtc"));
                break;
            case GB28181:
                LivestreamGb28181Url gb28181 = (LivestreamGb28181Url) url;
                live.setUrl(new StringBuilder()
                        .append("webrtc://")
                        .append(gb28181.getServerIP())
                        .append("/live/")
                        .append(gb28181.getAgentID())
                        .append("@")
                        .append(gb28181.getChannel())
                        .toString());
                break;
            case RTSP:
                live.setUrl(resolveRtspUrl(response.getData().getOutput()));
                break;
            case WHIP:
                live.setUrl(buildWhepUrl((LivestreamWhipUrl) url));
                break;
            default:
                return HttpResultResponse.error(LiveErrorCodeEnum.URL_TYPE_NOT_SUPPORTED);
        }

        return HttpResultResponse.success(live);
    }

    private String resolveRtspUrl(Object output) {
        if (output instanceof String) {
            return (String) output;
        }
        if (output instanceof Map<?, ?>) {
            Map<?, ?> outputMap = (Map<?, ?>) output;
            Object url = outputMap.get("url");
            return Objects.toString(url, "");
        }
        return Objects.toString(output, "");
    }

    private String buildWhepUrl(LivestreamWhipUrl whipUrl) {
        String url = whipUrl.getUrl();
        if (url.endsWith("/whip")) {
            return url.substring(0, url.length() - "/whip".length()) + "/whep";
        }
        if (url.endsWith("/whip/")) {
            return url.substring(0, url.length() - "/whip/".length()) + "/whep";
        }
        return url;
    }

    @Override
    public HttpResultResponse liveStop(VideoId videoId) {
        HttpResultResponse<DeviceDTO> responseResult = this.checkBeforeLive(videoId);
        if (HttpResultResponse.CODE_SUCCESS != responseResult.getCode()) {
            return responseResult;
        }

        TopicServicesResponse<ServicesReplyData> response = abstractLivestreamService.liveStopPush(
                SDKManager.getDeviceSDK(responseResult.getData().getDeviceSn()), new LiveStopPushRequest()
                        .setVideoId(videoId));
        if (!response.getData().getResult().isSuccess()) {
            return HttpResultResponse.error(response.getData().getResult());
        }

        return HttpResultResponse.success();
    }

    @Override
    public HttpResultResponse liveSetQuality(LiveTypeDTO liveParam) {
        HttpResultResponse<DeviceDTO> responseResult = this.checkBeforeLive(liveParam.getVideoId());
        if (responseResult.getCode() != 0) {
            return responseResult;
        }

        TopicServicesResponse<ServicesReplyData> response = abstractLivestreamService.liveSetQuality(
                SDKManager.getDeviceSDK(responseResult.getData().getDeviceSn()), new LiveSetQualityRequest()
                        .setVideoQuality(liveParam.getVideoQuality())
                        .setVideoId(liveParam.getVideoId()));
        if (!response.getData().getResult().isSuccess()) {
            return HttpResultResponse.error(response.getData().getResult());
        }

        return HttpResultResponse.success();
    }

    @Override
    public HttpResultResponse liveLensChange(LiveTypeDTO liveParam) {
        HttpResultResponse<DeviceDTO> responseResult = this.checkBeforeLive(liveParam.getVideoId());
        if (HttpResultResponse.CODE_SUCCESS != responseResult.getCode()) {
            return responseResult;
        }

        TopicServicesResponse<ServicesReplyData> response = abstractLivestreamService.liveLensChange(
                SDKManager.getDeviceSDK(responseResult.getData().getDeviceSn()), new LiveLensChangeRequest()
                        .setVideoType(liveParam.getVideoType())
                        .setVideoId(liveParam.getVideoId()));

        if (!response.getData().getResult().isSuccess()) {
            return HttpResultResponse.error(response.getData().getResult());
        }

        return HttpResultResponse.success();
    }

    /**
     * Check if this lens is available live.
     * @param videoId
     * @return
     */
    private HttpResultResponse<DeviceDTO> checkBeforeLive(VideoId videoId) {
        if (Objects.isNull(videoId)) {
            return HttpResultResponse.error(LiveErrorCodeEnum.ERROR_PARAMETERS);
        }

        Optional<DeviceDTO> deviceOpt = deviceService.getDeviceBySn(videoId.getDroneSn());
        // Check if the gateway device connected to this drone exists
        if (deviceOpt.isEmpty()) {
            return HttpResultResponse.error(LiveErrorCodeEnum.NO_AIRCRAFT);
        }

        if (DeviceDomainEnum.DOCK == deviceOpt.get().getDomain()) {
            return HttpResultResponse.success(deviceOpt.get());
        }
        List<DeviceDTO> gatewayList = deviceService.getDevicesByParams(
                DeviceQueryParam.builder()
                        .childSn(videoId.getDroneSn())
                        .build());
        if (gatewayList.isEmpty()) {
            return HttpResultResponse.error(LiveErrorCodeEnum.NO_FLIGHT_CONTROL);
        }

        return HttpResultResponse.success(gatewayList.get(0));
    }

    /**
     * This is business-customized logic and is only used for testing.
     * @param type
     * @param url
     * @param videoId
     */
    private ILivestreamUrl setExt(UrlTypeEnum type, ILivestreamUrl url, VideoId videoId) {
        switch (type) {
            case AGORA:
                LivestreamAgoraUrl agoraUrl = (LivestreamAgoraUrl) url.clone();
                return agoraUrl.setSn(videoId.getDroneSn());
            case RTMP:
                LivestreamRtmpUrl rtmpUrl = (LivestreamRtmpUrl) url.clone();
                return rtmpUrl.setUrl(rtmpUrl.getUrl() + videoId.getDroneSn() + "-" + videoId.getPayloadIndex().toString());
            case GB28181:
                String random = String.valueOf(Math.abs(videoId.getDroneSn().hashCode()) % 1000);
                LivestreamGb28181Url gbUrl = (LivestreamGb28181Url) url.clone();
                gbUrl.setAgentID(gbUrl.getAgentID().substring(0, 20 - random.length()) + random);
                String deviceType = String.valueOf(videoId.getPayloadIndex().getType().getType());
                return gbUrl.setChannel(gbUrl.getChannel().substring(0, 20 - deviceType.length()) + deviceType);
            case WHIP:
                LivestreamWhipUrl whipUrl = (LivestreamWhipUrl) url.clone();
                return whipUrl.setUrl(buildWhipPublishUrl(whipUrl.getUrl(), videoId));
        }
        return url;
    }

    private String buildWhipPublishUrl(String baseUrl, VideoId videoId) {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalizedBaseUrl + "/" + videoId.getDroneSn() + "-" + videoId.getPayloadIndex() + "/whip";
    }
}