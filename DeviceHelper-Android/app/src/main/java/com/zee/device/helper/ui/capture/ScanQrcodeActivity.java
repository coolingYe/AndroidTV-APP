package com.zee.device.helper.ui.capture;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.zee.device.base.config.BaseConstants;
import com.zee.device.base.utils.DensityUtils;
import com.zee.device.base.utils.ToastUtils;
import com.zee.device.helper.R;
import com.zee.device.helper.databinding.ActivityScanQrcodeBinding;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ScanQrcodeActivity extends AppCompatActivity {

    private ActivityScanQrcodeBinding binding;
    private BeepManager beepManager;
    private String lastScanResult = "";
    private long lastHandleTime = 0;

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null || TextUtils.isEmpty(result.getText())) {
                return;
            }

            if(result.getText().equals(lastScanResult) && (System.currentTimeMillis() - lastHandleTime <= 2000)){

            }else{
                lastScanResult = result.getText();
                lastHandleTime = System.currentTimeMillis();
                handleScanResult(result.getText());
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {

        }
    };

    private synchronized void handleScanResult(String scanResult) {
        if (!TextUtils.isEmpty(scanResult) && scanResult.startsWith(BaseConstants.QRCODE_CONTENT_PREFIX)) {
            String[] strArray = scanResult.split(";");
            if (strArray.length >= 6) {
                try {
                    Integer.parseInt(strArray[3]);
                    beepManager.playBeepSoundAndVibrate();
                    Intent intent = new Intent();
                    intent.putExtra(Intents.Scan.RESULT, scanResult);
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (NumberFormatException ignored) {
                    showToast("不支持该二维码！");
                }
            } else {
                showToast("不支持该二维码！");
            }
        } else {
            showToast("不支持该二维码！");
        }
    }

    public void showToast(String msg) {
        ToastUtils.showShort(msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scan_qrcode);

        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE);
        binding.barcodeScanner.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        binding.barcodeScanner.initializeFromIntent(getIntent());
        binding.barcodeScanner.decodeContinuous(callback);
        /*binding.barcodeScanner.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if(result != null && !TextUtils.isEmpty(result.getText())){
                    showToast(result.getText());
                }
            }
        });*/
        binding.barcodeScanner.setStatusText("");

        beepManager = new BeepManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.barcodeScanner.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ToastUtils.cancel();
        binding.barcodeScanner.pause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}