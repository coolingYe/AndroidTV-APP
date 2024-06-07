package com.zeewain.base.utils;

import android.content.Context;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

public class FontUtils {
    public static Typeface typeface;

    public static void initAssetFont(Context context, String fontFileName){
        typeface = Typeface.createFromAsset(context.getAssets(), fontFileName);
    }

    public static void initFont(Context context, int resourceId) {
        typeface = ResourcesCompat.getFont(context, resourceId);
    }
}
