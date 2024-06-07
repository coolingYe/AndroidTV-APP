package com.zee.device.helper.data.model;

import com.zee.device.base.model.DeviceInfo;

public class DataConnectionState {
    public ConnectionState connectionState;
    public ConnectionType connectionType;
    public String errMsg;
    public DeviceInfo deviceInfo;

    public DataConnectionState(ConnectionState connectionState, ConnectionType connectionType) {
        this.connectionState = connectionState;
        this.connectionType = connectionType;
    }

    public DataConnectionState(ConnectionState connectionState, ConnectionType connectionType, DeviceInfo deviceInfo) {
        this.connectionState = connectionState;
        this.connectionType = connectionType;
        this.deviceInfo = deviceInfo;
    }

    public void updateState(ConnectionState connectionState, String errMsg) {
        this.connectionState = connectionState;
        this.errMsg = errMsg;
    }

    public void updateState(ConnectionState connectionState, ConnectionType connectionType, String errMsg) {
        this.connectionState = connectionState;
        this.connectionType = connectionType;
        this.errMsg = errMsg;
    }
}
