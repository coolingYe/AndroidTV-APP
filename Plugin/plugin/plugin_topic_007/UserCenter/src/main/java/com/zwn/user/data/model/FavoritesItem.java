package com.zwn.user.data.model;

import java.util.Date;

public class FavoritesItem {
    public String url;
    public String title;
    public String summary;
    public String favoriteId;
    public String objId;
    public Date favoriteTime;

    public FavoritesItem(String url, String title, String summary, String favoriteId, String objId,
                         Date favoriteTime) {
        this.url = url;
        this.summary = summary;
        this.title = title;
        this.favoriteId = favoriteId;
        this.objId = objId;
        this.favoriteTime = favoriteTime;
    }
}
