package com.zeewain.base.trans;

public class CommunicationManager {
    private CommunicationListener mListener;

    private CommunicationManager() {
    }

    private static final CommunicationManager manager = new CommunicationManager();

    public static CommunicationManager getInstance() {
        return manager;
    }

    public void setConnectionStateListener(CommunicationListener mListener) {
        this.mListener = mListener;
    }

    public void deliver() {
        if (mListener != null) {
            mListener.deliver();
        }
    }


}