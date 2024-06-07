package com.zwn.user.data.protocol.response;

public class FavoritesResp {
    public String favoriteId;
    public String favoriteTime;
    public String objId;
    public String objName;
    public int objType;
    public String objUrl;
    public String objDesc;

    @Override
    public String toString() {
        return "FavoritesResp{" +
                "favoriteId='" + favoriteId + '\'' +
                ", favoriteTime='" + favoriteTime + '\'' +
                ", objId='" + objId + '\'' +
                ", objName='" + objName + '\'' +
                ", objType=" + objType +
                ", objUrl='" + objUrl + '\'' +
                ", objDesc='" + objDesc + '\'' +
                '}';
    }
}
