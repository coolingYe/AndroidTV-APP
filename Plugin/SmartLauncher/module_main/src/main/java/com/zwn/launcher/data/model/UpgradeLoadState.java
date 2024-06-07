package com.zwn.launcher.data.model;

import com.zeewain.base.model.LoadState;
import com.zeewain.base.data.protocol.response.UpgradeResp;

public class UpgradeLoadState {
    public LoadState loadState;
    public String softwareCode;
    public UpgradeResp upgradeResp;

    public UpgradeLoadState(LoadState loadState, String softwareCode) {
        this.loadState = loadState;
        this.softwareCode = softwareCode;
    }

    public UpgradeLoadState(LoadState loadState, String softwareCode, UpgradeResp upgradeResp) {
        this.loadState = loadState;
        this.softwareCode = softwareCode;
        this.upgradeResp = upgradeResp;
    }
}
