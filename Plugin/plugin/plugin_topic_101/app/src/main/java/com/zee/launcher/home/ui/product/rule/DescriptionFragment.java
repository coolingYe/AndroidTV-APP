package com.zee.launcher.home.ui.product.rule;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.zee.launcher.home.R;
import com.zeewain.base.utils.CommonUtils;

import java.util.Objects;

public class DescriptionFragment extends Fragment implements View.OnFocusChangeListener {
    public static final String TYPE_FROM = "type_from";
    public static final String TYPE_FROM_DESCRIPTION = "type_from_description";
    public static final String TYPE_FROM_RULE = "type_from_rule";

    private LinearLayout llContentFrame;
    private NestedScrollView nvContentFrame;

    public static DescriptionFragment newInstance(String keyFrom) {
        DescriptionFragment fragment = new DescriptionFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TYPE_FROM, keyFrom);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_description, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        llContentFrame = view.findViewById(R.id.ll_content_desc);
        nvContentFrame = view.findViewById(R.id.nv_content_desc);
        nvContentFrame.setNextFocusUpId(nvContentFrame.getId());
        nvContentFrame.setOnFocusChangeListener(this);
        View backView = view.findViewById(R.id.view_content_back);
        backView.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        backView.setOnFocusChangeListener(this);
        TextView tvContent = view.findViewById(R.id.tv_rule_desc);
        String content = "";
        if (getArguments() != null) {
            if (Objects.equals(getArguments().getString(TYPE_FROM), TYPE_FROM_DESCRIPTION)) {
                content = getString(R.string.event_description);
            } else if (Objects.equals(getArguments().getString(TYPE_FROM), TYPE_FROM_RULE)) {
                content = getString(R.string.event_rule);
            }
        }
        tvContent.setText(content);
        nvContentFrame.requestFocus();
    }

    @SuppressLint("ResourceType")
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == R.id.view_content_back) {
            if (hasFocus) {
                v.setBackgroundResource(com.zwn.user.R.mipmap.ic_back_selected_bg);
                CommonUtils.scaleView(v, 1.1f);

            } else {
                v.setBackgroundResource(com.zwn.user.R.mipmap.ic_back_bg);
                v.clearAnimation();
                CommonUtils.scaleView(v, 1f);
            }
        }

        if (v.getId() == R.id.nv_content_desc) {
            if (hasFocus) {
                llContentFrame.setBackgroundResource(R.mipmap.ic_desc_selected_frame);
            } else llContentFrame.setBackgroundResource(Color.TRANSPARENT);
        }
    }
}
