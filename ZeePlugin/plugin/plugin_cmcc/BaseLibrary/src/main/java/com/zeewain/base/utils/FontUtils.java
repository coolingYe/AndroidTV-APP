package com.zeewain.base.utils;

import android.content.Context;
import android.graphics.Typeface;


public class FontUtils {
    public static Typeface typefaceBold;
    public static Typeface typefaceHeavy;
    public static Typeface typefaceMedium;
    public static Typeface typefaceRegular;

    public static void initAssetFontBold(Context context, String fontFileName){
        typefaceBold = Typeface.createFromAsset(context.getAssets(), fontFileName);
    }

    public static void initAssetFontHeavy(Context context, String fontFileName){
        typefaceHeavy = Typeface.createFromAsset(context.getAssets(), fontFileName);
    }

    public static void initAssetFontMedium(Context context, String fontFileName) {
        typefaceMedium = Typeface.createFromAsset(context.getAssets(), fontFileName);
    }

    public static void initAssetFontRegular(Context context, String fontFileName) {
        typefaceRegular = Typeface.createFromAsset(context.getAssets(), fontFileName);
    }
}
