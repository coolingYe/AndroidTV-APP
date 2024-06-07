package com.zeewain.base.data.protocol.response;

public class BaseResp<T> {
    public int code;
    public String message;
    public boolean isCache;
    public T data;
}
