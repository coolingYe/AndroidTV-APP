package com.zwn.user.data.model;

import com.zeewain.base.model.LoadState;

public class SessionResultLoadState {
    public LoadState loadState;
    public SessionInfoMo sessionInfoMo;
    public String token;
    public int errCode;

    public SessionResultLoadState(LoadState loadState, SessionInfoMo sessionInfoMo) {
        this.loadState = loadState;
        this.sessionInfoMo = sessionInfoMo;
    }

    public SessionResultLoadState(LoadState loadState, SessionInfoMo sessionInfoMo, String token) {
        this.loadState = loadState;
        this.sessionInfoMo = sessionInfoMo;
        this.token = token;
    }

    public SessionResultLoadState(LoadState loadState, SessionInfoMo sessionInfoMo, int errCode) {
        this.loadState = loadState;
        this.sessionInfoMo = sessionInfoMo;
        this.errCode = errCode;
    }
}
