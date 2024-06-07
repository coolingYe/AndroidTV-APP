package com.zeewain.base.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.zeewain.base.R;
import com.zeewain.base.config.SharePrefer;

public class CommonVariableCacheUtils {
    private static final String TAG = "CommonVariableCacheUtil";

    private static CommonVariableCacheUtils instance;
    public static CommonVariableCacheUtils getInstance() {
        if (instance == null) {
            instance = new CommonVariableCacheUtils();
            Log.d(TAG, "CommonVariableCacheUtils init");
        }
        return instance;
    }

    public String token;

    public void initToken() {
        token = SPUtils.getInstance().getString(SharePrefer.userToken);
        if (token == null) {
            token = "";
        }
    }

    private RequestOptions options13;
    private RequestOptions options5;

    public void initOptions(Resources resources) {
        options13 = new RequestOptions()
                .transform(new MultiTransformation<>(
                        new CenterCrop(),
                        new RoundedCorners(resources.getDimensionPixelSize(R.dimen.src_dp_13))
                ));
        options5 = new RequestOptions()
                .transform(new MultiTransformation<>(
                        new CenterCrop(),
                        new RoundedCorners(resources.getDimensionPixelSize(R.dimen.src_dp_5))
                ));
    }

    public RequestOptions getOptions13() {
        return options13;
    }

    public RequestOptions getOptions5() {
        return options5;
    }

    private RoundedBitmapDrawable loadingDrawable26;
    private RoundedBitmapDrawable loadFailedDrawable26;

    public void initDrawable(Resources resources) {
        Bitmap loadingImage = BitmapFactory.decodeResource(resources, R.mipmap.img_loading, null);
        loadingDrawable26 = RoundedBitmapDrawableFactory.create(resources, loadingImage);
        loadingDrawable26.setCornerRadius(resources.getDimension(R.dimen.src_dp_26));

        Bitmap loadFailedImage = BitmapFactory.decodeResource(resources, R.mipmap.img_load_failed, null);
        loadFailedDrawable26 = RoundedBitmapDrawableFactory.create(resources, loadFailedImage);
        loadFailedDrawable26.setCornerRadius(resources.getDimension(R.dimen.src_dp_26));
    }

    public RoundedBitmapDrawable getLoadingDrawable26() {
        return loadingDrawable26;
    }

    public RoundedBitmapDrawable getLoadFailedDrawable26() {
        return loadFailedDrawable26;
    }
}
