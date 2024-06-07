package com.zee.launcher.home.ui.helong;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;

import com.zee.launcher.home.HomeApplication;
import com.zee.launcher.home.R;
import com.zee.launcher.home.dialog.CameraAlertDialog;
import com.zee.launcher.home.gesture.config.Config;
import com.zee.launcher.home.service.GestureCameraService;
import com.zee.launcher.home.widgets.BackView;
import com.zee.launcher.home.widgets.curl.CurlPage;
import com.zee.launcher.home.widgets.curl.CurlView;
import com.zee.launcher.home.widgets.curl.model.TouchAction;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CareLog;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PageTurningActivity extends BaseActivity implements GestureCameraService.GestureListener, CameraAlertDialog.OnClickListener {
    private final static String TAG = "PageTurningActivity";
    private CurlView curlView;
    private BackView backView;
    private final String[] imgArray = {"hl_info_5.jpg", "hl_info_1.jpg", "hl_info_2.jpg", "hl_info_3.jpg", "hl_info_4.jpg", "hl_info_5.jpg", "hl_info_1.jpg"};
    private final ArrayList<String> imgList = new ArrayList<>();
    private final HashMap<Integer, Bitmap> bitmapHashMap = new HashMap<>(5);
    private final Handler mHandler = new Handler(Looper.myLooper());
    private CameraAlertDialog cameraAlertDialog;
    private long onCreateTime = 0;

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.setClassLoader(getClass().getClassLoader());
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            savedInstanceState.setClassLoader(getClass().getClassLoader());
            Bundle bundle = savedInstanceState.getBundle("androidx.lifecycle.BundlableSavedStateRegistry.key");
            if(bundle != null) {
                Set<String> keySet = bundle.keySet();
                if (keySet != null) {
                    for(String key: keySet){
                        Object object = bundle.get(key);
                        if(object instanceof Bundle){
                            ((Bundle)object).setClassLoader(getClass().getClassLoader());
                        }
                    }
                }
            }
        }
        if(savedInstanceState != null) {
            savedInstanceState.setClassLoader(getClass().getClassLoader());
            Bundle bundle = savedInstanceState.getBundle("androidx.lifecycle.BundlableSavedStateRegistry.key");
            if(bundle != null) {
                Set<String> keySet = bundle.keySet();
                if (keySet != null) {
                    for(String key: keySet){
                        Object object = bundle.get(key);
                        if(object instanceof Bundle){
                            ((Bundle)object).setClassLoader(getClass().getClassLoader());
                        }
                    }
                }
            }
        }
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_page_turning);

        backView = findViewById(R.id.back_view_page_turning);
        backView.setOnClickListener(v -> delayFinish());

        backView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                CommonUtils.scaleView(v, 1.1f);
            } else {
                v.clearAnimation();
                CommonUtils.scaleView(v, 1f);
            }
        });

        backView.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake));
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake_y));
            }
            return false;
        });

        curlView = findViewById(R.id.curl_view);
        curlView.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                prevAction();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                nextAction();
            }
            return false;
        });

        curlView.postDelayed(() -> {
            curlView.setVisibility(View.VISIBLE);
            curlView.requestLayout();
            curlView.onPageSizeChanged(curlView.getWidth(), curlView.getHeight());
            curlView.requestFocus();
        }, 400);
        initImage();

        onCreateTime = System.currentTimeMillis();
    }

    private synchronized void nextAction() {
        if(View.VISIBLE != curlView.getVisibility()) return;
        if(System.currentTimeMillis() - lastOnFlippingTime < 800) return;
        lastOnFlippingTime = System.currentTimeMillis();
        List<TouchAction> touchActionList = new ArrayList<>();

        float range = (curlView.getWidth() * 3f / 4) / 20;
        for (int i = 0; i < 20; i++) {
            if (i == 0) {
                touchActionList.add(new TouchAction(MotionEvent.ACTION_DOWN, curlView.getWidth(), curlView.getHeight() / 2f));
            } else if (i == 19) {
                touchActionList.add(new TouchAction(MotionEvent.ACTION_UP, curlView.getWidth() - i * range, curlView.getHeight() / 2f));
            } else {
                touchActionList.add(new TouchAction(MotionEvent.ACTION_MOVE, curlView.getWidth() - i * range, curlView.getHeight() / 2f));
            }
        }

        for (int i = 0; i < touchActionList.size(); i++) {
            handleCurlViewTouch(touchActionList.get(i), i * 40);
        }
    }

    private synchronized void prevAction() {
        if(View.VISIBLE != curlView.getVisibility()) return;
        if(System.currentTimeMillis() - lastOnFlippingTime < 800) return;
        lastOnFlippingTime = System.currentTimeMillis();
        List<TouchAction> touchActionList = new ArrayList<>();
        float range = (curlView.getWidth() * 3f / 4) / 20;
        for (int i = 0; i < 20; i++) {
            if (i == 0) {
                touchActionList.add(new TouchAction(MotionEvent.ACTION_DOWN, 0, curlView.getHeight() / 2f));
            } else if (i == 19) {
                touchActionList.add(new TouchAction(MotionEvent.ACTION_UP, i * range, curlView.getHeight() / 2f));
            } else {
                touchActionList.add(new TouchAction(MotionEvent.ACTION_MOVE, i * range, curlView.getHeight() / 2f));
            }
        }

        for (int i = 0; i < touchActionList.size(); i++) {
            handleCurlViewTouch(touchActionList.get(i), i * 40);
        }
    }

    private long lastOnFlippingTime = 0;

    private void handleCurlViewTouch(final TouchAction touchAction, final long delay) {
        curlView.postDelayed(() -> curlView.onTouchAction(touchAction), delay);
    }

    private void initImage() {
        String dir = getExternalFilesDir("HeLong").toString() + "/";
        boolean success = FileUtils.copyFilesFromAssetsTo(this, imgArray, dir);
        if (success) {
            for (String imgName : imgArray) {
                String imgPath = dir + imgName;
                imgList.add(imgPath);
            }
            curlView.setPageProvider(new PageProvider(imgList));
            curlView.setSizeChangedObserver(new SizeChangedObserver());
            curlView.setCurrentIndex(1);
            curlView.setBackgroundColor(Color.TRANSPARENT);
            curlView.setAllowLastPageCurl(false);
            curlView.setPageChanged(new CurlView.PageChanged() {
                @Override
                public void onPageChanged(int index) {
                    CareLog.i(TAG, "onPageChanged() index=" + index);
                    if(curlView.getCurrentIndex() == imgArray.length -1){
                        curlView.setCurrentIndex(1);
                    }else if(curlView.getCurrentIndex() == 0){
                        curlView.setCurrentIndex(imgArray.length -2);
                    }
                }
            });
        } else {
            showToast("拷贝文件失败！");
            delayFinish();
        }
    }


    /**
     * Bitmap provider.
     */
    private class PageProvider implements CurlView.PageProvider {
        private final ArrayList<String> mPathArray;

        public PageProvider(ArrayList<String> pathArray) {
            mPathArray = pathArray;
        }

        @Override
        public int getPageCount() {
            return mPathArray.size();
        }

        private Bitmap loadBitmap(int width, int height, int index) {
            long currentTime = System.currentTimeMillis();
            CareLog.i(TAG, "loadBitmap() currentTime=" + currentTime);
            CareLog.i(TAG, "loadBitmap() width=" + width + ", height=" + height + ", index=" + index);
            Bitmap b = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);
            b.eraseColor(0x00000000);
            Canvas c = new Canvas(b);
            //Drawable d = getResources().getDrawable(mBitmapIds[index]);
            Bitmap image = BitmapFactory.decodeFile(mPathArray.get(index));

            BitmapDrawable d = new BitmapDrawable(getResources(), image);

            int margin = 0;
            int border = 1;
            Rect r = new Rect(margin, margin, width - margin, height - margin);

            int imageWidth = r.width() - (border * 2);
            int imageHeight = imageWidth * d.getIntrinsicHeight()
                    / d.getIntrinsicWidth();
            if (imageHeight > r.height() - (border * 2)) {
                imageHeight = r.height() - (border * 2);
                imageWidth = imageHeight * d.getIntrinsicWidth()
                        / d.getIntrinsicHeight();
            }

            r.left += ((r.width() - imageWidth) / 2) - border;
            r.right = r.left + imageWidth + border + border;
            r.top += ((r.height() - imageHeight) / 2) - border;
            r.bottom = r.top + imageHeight + border + border;

            Paint p = new Paint();
            p.setColor(0x00C0C0C0);
            c.drawRect(r, p);
            r.left += border;
            r.right -= border;
            r.top += border;
            r.bottom -= border;

            d.setBounds(r);
            d.draw(c);
            CareLog.i(TAG, "loadBitmap() cost time=" + (System.currentTimeMillis() - currentTime));
            return b;
        }

        @Override
        public void updatePage(CurlPage page, int width, int height, int index) {
            CareLog.i(TAG, "updatePage() width=" + width + ", height=" + height + ", index=" + index);
            if (!bitmapHashMap.containsKey(index)) {
                Bitmap front = loadBitmap(width, height, index);
                bitmapHashMap.put(index, front);
            }
            Bitmap front = bitmapHashMap.get(index).copy(Bitmap.Config.ARGB_8888, false);
            page.setTexture(front, CurlPage.SIDE_BOTH);
        }
    }

    private void showCameraAlert(int errorType) {
        cameraAlertDialog = new CameraAlertDialog(this);
        cameraAlertDialog.setOnClickListener(this);
        cameraAlertDialog.setErrorType(errorType);
        cameraAlertDialog.show();
    }


    /**
     * CurlView size changed observer.
     */
    private class SizeChangedObserver implements CurlView.SizeChangedObserver {
        @Override
        public void onSizeChanged(int w, int h) {
            curlView.setViewMode(CurlView.SHOW_ONE_PAGE);
            curlView.setMargins(0f, 0f, 0f, 0f);
        }
    }

    private void delayFinish(){
        curlView.setAlpha(0);
        curlView.postDelayed(() -> {
            curlView.setVisibility(View.GONE);
            finish();
        }, 200);
    }

    @Override
    public void onBackPressed() {
        delayFinish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        curlView.postDelayed(new Runnable() {
            @Override
            public void run() {
                HomeApplication.getInstance().gestureCameraService.setGestureListener(PageTurningActivity.this, 1);
            }
        }, 1200);
    }

    @Override
    protected void onDestroy() {
        if(curlView !=null && curlView.getHandler() != null)
            curlView.getHandler().removeCallbacksAndMessages(null);
        for (Map.Entry<Integer, Bitmap> entry : bitmapHashMap.entrySet()) {
            entry.getValue().recycle();
        }
        bitmapHashMap.clear();
        super.onDestroy();
    }

    @Override
    public void onLeftHandUpProgress(int progress) {

    }

    @Override
    public void onRightHandUpProgress(int progress) {

    }

    @Override
    public void onExpandProgress(int progress) {

    }

    @Override
    public void onThrowbackProgress(int progress) {
        backView.startLoading(progress);
        if (progress == GestureCameraService.backProgress) {
            delayFinish();
        }
    }

    @Override
    public void onSlipLeft() {
        if(System.currentTimeMillis() - onCreateTime > 1200) {
            prevAction();
        }
    }

    @Override
    public void onSlipRight() {
        if(System.currentTimeMillis() - onCreateTime > 1200) {
            nextAction();
        }
    }

    @Override
    public void onError(int errType) {
        runOnUiThread(() -> showCameraAlert(errType));
    }

    @Override
    public void onConfirm(View v, int errType) {
        switch (errType) {
            case Config.CameraError_EMPTY:
                break;
            case Config.CameraError_INVALID:
            case Config.CameraError_ERROR:
                mHandler.postDelayed(() -> HomeApplication.getInstance().gestureCameraService.resumeHideGesture(), 1000);
                break;
            case Config.CameraError_UnKNOW:
                break;
        }
    }

}