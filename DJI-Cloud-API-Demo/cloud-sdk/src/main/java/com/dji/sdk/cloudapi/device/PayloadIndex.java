package com.dji.sdk.cloudapi.device;

import com.dji.sdk.exception.CloudSDKErrorEnum;
import com.dji.sdk.exception.CloudSDKException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author sean
 * @version 1.7
 * @date 2023/6/29
 */
public class PayloadIndex {

    @NotNull
    private DeviceTypeEnum type;

    @NotNull
    private DeviceSubTypeEnum subType;

    @NotNull
    private PayloadPositionEnum position;

    // Store raw integer values so toString() round-trips correctly even for unknown types
    private int rawType;
    private int rawSubType;
    private int rawPosition;

    public PayloadIndex() {
    }

    @JsonCreator
    public PayloadIndex(String payloadIndex) {
        Objects.requireNonNull(payloadIndex);
        int[] payloadIndexArr = Arrays.stream(payloadIndex.split("-")).mapToInt(Integer::parseInt).toArray();
        if (payloadIndexArr.length != 3) {
            throw new CloudSDKException(CloudSDKErrorEnum.INVALID_PARAMETER);
        }
        this.rawType = payloadIndexArr[0];
        this.rawSubType = payloadIndexArr[1];
        this.rawPosition = payloadIndexArr[2];
        this.type = DeviceTypeEnum.find(rawType);
        this.subType = DeviceSubTypeEnum.find(rawSubType);
        this.position = PayloadPositionEnum.find(rawPosition);
    }

    @Override
    @JsonValue
    public String toString() {
        return String.format("%d-%d-%d", rawType, rawSubType, rawPosition);
    }

    public DeviceTypeEnum getType() {
        return type;
    }

    public PayloadIndex setType(DeviceTypeEnum type) {
        this.type = type;
        this.rawType = type.getType();
        return this;
    }

    public DeviceSubTypeEnum getSubType() {
        return subType;
    }

    public PayloadIndex setSubType(DeviceSubTypeEnum subType) {
        this.subType = subType;
        this.rawSubType = subType.getSubType();
        return this;
    }

    public PayloadPositionEnum getPosition() {
        return position;
    }

    public PayloadIndex setPosition(PayloadPositionEnum position) {
        this.position = position;
        this.rawPosition = position.getPosition();
        return this;
    }
}
