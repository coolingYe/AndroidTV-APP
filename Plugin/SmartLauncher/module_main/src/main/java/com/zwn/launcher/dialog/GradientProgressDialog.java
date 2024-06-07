package com.zwn.launcher.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.zeewain.base.widgets.GradientProgressView;
import com.zwn.launcher.R;


public class GradientProgressDialog extends Dialog {
    private TextView titleTextView;
    private TextView progressTextView;
    private TextView positiveView;
    private TextView cancelView;
    private LinearLayout positiveCancelLayout;
    private GradientProgressView gradientProgressView;
    private OnClickListener onClickListener;

    public GradientProgressDialog(@NonNull Context context) {
        this(context, R.style.UpgradeDialog);
    }

    public GradientProgressDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_gradient_progress);
        setCanceledOnTouchOutside(false);

        titleTextView = findViewById(R.id.txt_title_progress);
        progressTextView = findViewById(R.id.txt_value_progress);

        positiveView = findViewById(R.id.txt_positive_progress);
        positiveView.setOnClickListener(v -> {
            if(onClickListener != null){
                onClickListener.onPositive(v);
            }
        });

        cancelView = findViewById(R.id.txt_cancel_progress);
        cancelView.setOnClickListener(v -> {
            if(onClickListener != null){
                onClickListener.onCancel(v);
            }
        });

        positiveCancelLayout = findViewById(R.id.ll_positive_cancel_progress);

        gradientProgressView = findViewById(R.id.gradient_progress_view);
    }

    @SuppressLint("DefaultLocale")
    public void setProgress(int progress){
        if(gradientProgressView != null) {
            gradientProgressView.setProgress(progress);
            progressTextView.setText(String.format("%d/100", progress));
        }
    }

    public void setTitleText(String titleText){
        if(titleTextView != null){
            titleTextView.setText(titleText);
        }
    }

    public void showPositiveCancelButton(){
        positiveCancelLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void show() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        fullScreenImmersive(getWindow().getDecorView());
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    private void fullScreenImmersive(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            view.setSystemUiVisibility(uiOptions);
        }
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onPositive(View v);
        void onCancel(View v);
    }
}
