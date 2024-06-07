package com.zee.launcher.home.ui.home.model;


import com.zee.launcher.home.data.layout.AppCardListLayout;
import com.zee.launcher.home.data.layout.SwiperLayout;

public class ProductListType {
    public static final int TYPE_MASTER = 1;
    public static final int TYPE_BANNER = 10;
    public static final int TYPE_SLAVE = 20;
    public static final int TYPE_CLASSIC_LIST = 30;
    public static final int TYPE_CLASSIC_MODULE = 40;
    public static final int TYPE_FOOTER = 100;

    public int type;
    public String careKey;
    public String listTitle;
    public AppCardListLayout appCardListLayout;
    public SwiperLayout swiperLayout;

    public ProductListType(int type, String careKey) {
        this.type = type;
        this.careKey = careKey;
    }

    public ProductListType(int type, String careKey, String listTitle) {
        this.type = type;
        this.careKey = careKey;
        this.listTitle = listTitle;
    }

    public boolean isShowListTitle(){
        if(listTitle != null && !listTitle.isEmpty()){
            return true;
        }
        return false;
    }
}
