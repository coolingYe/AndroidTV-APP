package com.zee.launcher.home.data.protocol.response;

public class ShowBannerBean {
    public String kind;
    public String url;
    public String title;
    public String play;
    public String skuId;

    public ShowBannerBean(String kind, String url) {
        this.kind = kind;
        this.url = url;
    }

    public ShowBannerBean(String kind, String url, String title, String skuId) {
        this.kind = kind;
        this.url = url;
        this.title = title;
        this.skuId = skuId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getPlay() {
        return play;
    }

    public void setPlay(String play) {
        this.play = play;
    }


    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }
}
