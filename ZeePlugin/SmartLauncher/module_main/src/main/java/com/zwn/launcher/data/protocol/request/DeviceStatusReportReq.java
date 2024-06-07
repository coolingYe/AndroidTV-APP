package com.zwn.launcher.data.protocol.request;

import java.util.ArrayList;
import java.util.List;

public class DeviceStatusReportReq {
    public String deviceSn;

    public List<DeviceStatusErrorInfo> errorList = new ArrayList<>(0);

    public DeviceStatusReportReq(String deviceSn) {
        this.deviceSn = deviceSn;
    }
}
