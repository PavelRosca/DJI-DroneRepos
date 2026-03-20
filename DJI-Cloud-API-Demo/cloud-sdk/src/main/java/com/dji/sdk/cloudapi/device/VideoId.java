package com.dji.sdk.cloudapi.device;

import com.dji.sdk.cloudapi.livestream.VideoTypeEnum;
import com.dji.sdk.exception.CloudSDKErrorEnum;
import com.dji.sdk.exception.CloudSDKException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * @author sean
 * @version 1.7
 * @date 2023/6/25
 */
public class VideoId {

    @NotNull
    private String droneSn;

    @NotNull
    private PayloadIndex payloadIndex;

    @NotNull
    private VideoTypeEnum videoType = VideoTypeEnum.NORMAL;

    public VideoId() {
    }

    @JsonCreator
    public VideoId(Object videoId) {
        if (videoId instanceof Map<?, ?>) {
            Map<?, ?> mapValue = (Map<?, ?>) videoId;
            this.parseFromMap(mapValue);
            return;
        }
        this.parseFromString(Objects.toString(videoId, ""));
    }

    private void parseFromString(String videoId) {
        if (!StringUtils.hasText(videoId)) {
            return;
        }
        String[] videoIdArr = Arrays.stream(videoId.split("/")).toArray(String[]::new);
        if (videoIdArr.length != 3) {
            throw new CloudSDKException(CloudSDKErrorEnum.INVALID_PARAMETER);
        }
        this.droneSn = videoIdArr[0];
        this.payloadIndex = new PayloadIndex(videoIdArr[1]);
        this.videoType = VideoTypeEnum.find(videoIdArr[2].split("-")[0]);
    }

    private void parseFromMap(Map<?, ?> mapValue) {
        Object droneSnValue = firstNonNull(mapValue.get("drone_sn"), mapValue.get("droneSn"));
        Object payloadIndexValue = firstNonNull(mapValue.get("payload_index"), mapValue.get("payloadIndex"));
        Object videoTypeValue = firstNonNull(mapValue.get("video_type"), mapValue.get("videoType"));

        this.droneSn = Objects.toString(droneSnValue, "");
        this.payloadIndex = parsePayloadIndex(payloadIndexValue);

        if (videoTypeValue != null) {
            this.videoType = VideoTypeEnum.find(Objects.toString(videoTypeValue, ""));
        }
    }

    private PayloadIndex parsePayloadIndex(Object payloadIndexValue) {
        if (payloadIndexValue == null) {
            return null;
        }
        if (payloadIndexValue instanceof String) {
            String payloadIndexText = (String) payloadIndexValue;
            return new PayloadIndex(payloadIndexText);
        }
        if (!(payloadIndexValue instanceof Map<?, ?>)) {
            return new PayloadIndex(Objects.toString(payloadIndexValue, ""));
        }
        Map<?, ?> payloadIndexMap = (Map<?, ?>) payloadIndexValue;

        Object typeValue = firstNonNull(payloadIndexMap.get("type"), payloadIndexMap.get("payload_type"));
        Object subTypeValue = firstNonNull(payloadIndexMap.get("sub_type"), payloadIndexMap.get("subType"));
        Object positionValue = firstNonNull(payloadIndexMap.get("position"), payloadIndexMap.get("payload_position"));

        int type = parseIntValue(typeValue);
        int subType = parseIntValue(subTypeValue);
        int position = parseIntValue(positionValue);

        return new PayloadIndex(type + "-" + subType + "-" + position);
    }

    private int parseIntValue(Object value) {
        if (value instanceof Number) {
            Number number = (Number) value;
            return number.intValue();
        }
        return Integer.parseInt(Objects.toString(value, "0"));
    }

    private Object firstNonNull(Object first, Object second) {
        return first != null ? first : second;
    }

    @Override
    @JsonValue
    public String toString() {
        if (Objects.isNull(payloadIndex)) {
            return "";
        }
        return String.format("%s/%s/%s-0", droneSn, payloadIndex.toString(), videoType.getType());
    }

    public String getDroneSn() {
        return droneSn;
    }

    public VideoId setDroneSn(String droneSn) {
        this.droneSn = droneSn;
        return this;
    }

    public PayloadIndex getPayloadIndex() {
        return payloadIndex;
    }

    public VideoId setPayloadIndex(PayloadIndex payloadIndex) {
        this.payloadIndex = payloadIndex;
        return this;
    }

    public VideoTypeEnum getVideoType() {
        return videoType;
    }

    public VideoId setVideoType(VideoTypeEnum videoType) {
        this.videoType = videoType;
        return this;
    }
}
