package com.zwn.lib_download.model;

public class LoadedInfo {

    public String fileId;

    public long loadedSize;

    public LoadedInfo(String fileId, long loadedSize) {
        this.fileId = fileId;
        this.loadedSize = loadedSize;
    }
}
