package com.zee.launcher.home.config;


import com.zee.launcher.home.R;

import java.util.HashMap;
import java.util.Map;

public class ProdConstants {
    public static  int DEFAULT_FOCUS_INDEX = 1;
    public static final String LATEST_INTERACT_ITEM = "LatestInteractItem";
    public static final String MINE_HEADER = "MineHeader";
    public static final String INTERACTIVE_RECORD = "InteractiveRecord";
    public static final String MY_FAVORITES = "MyFavorites";
    public static final String MY_DOWNLOADS = "MyDownloads";
    public static final String VERSION_UPDATE = "VersionUpdate";
    public static final String ABOUT_US = "AboutUs";
    public static final String MINE_PRODUCT = "Product";

    public static class Module{
        //1-党建普法  2-科普创新  3-教育培训  4-基层治理 4-1 AR防电诈 4-2 AR电诈信息识别 4-2 电诈案例播报 5-幸福健康  6-工会之家
        public static final String TYPE_1 = "党建";
        public static final String TYPE_2 = "创新";
        public static final String TYPE_3 = "培训";
        public static final String TYPE_4_1 = "AR防电诈知识竞答";
        public static final String TYPE_4_2 = "AR防电诈信息识别";
        public static final String TYPE_4_3 = "电诈案例播报";
        public static final String TYPE_5 = "康乐";
        public static final String TYPE_6 = "团结";
    }

    public static Map<String, Integer> CategoryIconMap = new HashMap<String, Integer>() {
        {
            put(Module.TYPE_1, R.mipmap.icon_home_tab_type_1);
            put(Module.TYPE_2, R.mipmap.icon_home_tab_type_2);
            put(Module.TYPE_3, R.mipmap.icon_home_tab_type_3);
            put(Module.TYPE_4_1, R.mipmap.icon_home_tab_type_4_1);
            put(Module.TYPE_4_2, R.mipmap.icon_home_tab_type_4_2);
            put(Module.TYPE_4_3, R.mipmap.icon_home_tab_type_4_3);
            put(Module.TYPE_5, R.mipmap.icon_home_tab_type_5);
            put(Module.TYPE_6, R.mipmap.icon_home_tab_type_6);
        }
    };

    public static final int PRD_PAGE_SIZE = 100;

    public static final String SKU_ID_LOADING = "-1";
    public static final String SKU_ID_LOADED_ERR = "-2";

}
