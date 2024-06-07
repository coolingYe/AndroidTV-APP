package com.zee.launcher.home.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zee.launcher.home.data.layout.AppCardListLayout;
import com.zee.launcher.home.data.layout.CategoryBarLayout;
import com.zee.launcher.home.data.layout.GlobalLayout;
import com.zee.launcher.home.data.layout.PageHeaderLayout;
import com.zee.launcher.home.data.layout.PageLayoutDTO;
import com.zee.launcher.home.data.layout.SwiperLayout;

import java.util.ArrayList;
import java.util.List;

public class GlobalLayoutHelper {

    public static GlobalLayout analysisGlobalLayout(JSONObject globalLayoutJsonObject) {
        GlobalLayout globalLayout =  JSON.toJavaObject(globalLayoutJsonObject, GlobalLayout.class);

        if(globalLayout == null || globalLayout.layout == null || globalLayout.layout.basic == null || globalLayout.layout.pages == null){
            return null;
        }

        JSONArray jSONArray = globalLayout.layout.basic.content;
        if(jSONArray != null){
            for(int i=0; i<jSONArray.size(); i++){
                JSONObject jsonObject = (JSONObject)jSONArray.get(i);
                String uid = jsonObject.getString("uid");
                if("page-header".equals(uid)){
                    PageHeaderLayout pageHeaderLayout = JSON.toJavaObject(jsonObject, PageHeaderLayout.class);
                    globalLayout.layout.basic.pageHeaderLayout = pageHeaderLayout;
                }else if("app-category-bar".equals(uid)){
                    CategoryBarLayout categoryBarLayout = JSON.toJavaObject(jsonObject, CategoryBarLayout.class);
                    globalLayout.layout.basic.categoryBarLayout = categoryBarLayout;
                }
            }
        }

        for(int i=0; i<globalLayout.layout.pages.size(); i++){
            PageLayoutDTO pagesDTO = globalLayout.layout.pages.get(i);

            if(pagesDTO.content != null && pagesDTO.content.size() >= 1){
                handJsonArrayPageContent(pagesDTO, pagesDTO.content);
            }
        }

        return globalLayout;
    }

    private static void handJsonArrayPageContent(PageLayoutDTO pagesDTO, JSONArray jSONArrayPageContent){
        if(jSONArrayPageContent != null){
            for(int j=0; j<jSONArrayPageContent.size(); j++){
                JSONObject jsonObject = (JSONObject)jSONArrayPageContent.get(j);
                String uid = jsonObject.getString("uid");
                if (uid.contains("swiper")) {
                    pagesDTO.swiperLayout = JSON.toJavaObject(jsonObject, SwiperLayout.class);
                }else if("app-list-1".equals(uid)){
                    AppCardListLayout appCardListLayout = JSON.toJavaObject(jsonObject, AppCardListLayout.class);
                    pagesDTO.appCardListLayoutList.add(0, appCardListLayout);
                }else if("app-list-2".equals(uid)){
                    AppCardListLayout appCardListLayout = JSON.toJavaObject(jsonObject, AppCardListLayout.class);
                    pagesDTO.appCardListLayoutList.add(appCardListLayout);
                }
            }
        }
    }

    public static List<String> filterAPP(List<SwiperLayout.Item> mixedBanners) {
        List<String> appSkus = new ArrayList<>();
        mixedBanners.forEach(item -> {
            if (item.skuId != null) {
                appSkus.add(item.skuId);
            }
        });
        return appSkus;
    }
}
