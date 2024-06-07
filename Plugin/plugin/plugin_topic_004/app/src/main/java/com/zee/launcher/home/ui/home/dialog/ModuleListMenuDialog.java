package com.zee.launcher.home.ui.home.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;
import com.zee.launcher.home.ui.home.model.ModuleMenuMo;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;

import java.util.ArrayList;
import java.util.List;

public class ModuleListMenuDialog extends Dialog {

    private OnMenuSelectListener onMenuSelectListener;

    private List<ModuleMenuMo> dataList = new ArrayList<>();

    public ModuleListMenuDialog(@NonNull Context context) {
        this(context, R.style.ModuleListMenuDialog);
    }

    public ModuleListMenuDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_module_list_layout);
        RecyclerView menuRecyclerView = findViewById(R.id.recycler_view_module_list_menu);
        menuRecyclerView.setAdapter(new ModuleListMenuAdapter(dataList, onMenuSelectListener));

        DividerItemDecoration divider = new DividerItemDecoration(menuRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        Drawable drawable = ContextCompat.getDrawable(menuRecyclerView.getContext(), R.drawable.divider_module_list_menu);
        if(drawable != null)
            divider.setDrawable(drawable);

        menuRecyclerView.addItemDecoration(divider);
        initView(menuRecyclerView.getContext());
    }

    private void initView(Context context){
        Window window = getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        setCanceledOnTouchOutside(true);
        setCancelable(true);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width= ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height= ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.x = (int)DensityUtils.WIDTH - DisplayUtil.dip2px(context, 70);
        layoutParams.y = (int)DensityUtils.HEIGHT - DisplayUtil.dip2px(context, 120 + 28*dataList.size());
        getWindow().setAttributes(layoutParams);
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

    public void setDataList(List<ModuleMenuMo> dataList) {
        this.dataList = dataList;
    }

    public void setOnMenuSelectListener(OnMenuSelectListener onMenuSelectListener) {
        this.onMenuSelectListener = onMenuSelectListener;
    }
}
