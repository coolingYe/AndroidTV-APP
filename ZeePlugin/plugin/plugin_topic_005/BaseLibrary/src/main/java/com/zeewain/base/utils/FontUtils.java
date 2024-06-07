package com.zeewain.base.utils;

import android.content.Context;
import android.graphics.Typeface;


public class FontUtils {
    public static Typeface typeface;

    public static void initAssetFont(Context context, String fontFileName){
        typeface = Typeface.createFromAsset(context.getAssets(), fontFileName);
    }
}
