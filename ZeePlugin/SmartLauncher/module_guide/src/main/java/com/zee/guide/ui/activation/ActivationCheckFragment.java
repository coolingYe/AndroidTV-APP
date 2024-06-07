package com.zee.guide.ui.activation;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.zee.guide.R;
import com.zee.guide.ui.GuideViewModel;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;


public class ActivationCheckFragment extends Fragment implements View.OnFocusChangeListener {
    private GuideViewModel viewModel;

    public ActivationCheckFragment() { }

    public static ActivationCheckFragment newInstance() {
        return new ActivationCheckFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(GuideViewModel.class);
        View view = inflater.inflate(R.layout.fragment_activation_check, container, false);
        TextView textActivationCheckEnter = view.findViewById(R.id.txt_activation_check_enter);
        MaterialCardView cardActivationCheckEnter = view.findViewById(R.id.card_activation_check_enter);
        cardActivationCheckEnter.setOnFocusChangeListener(this);
        cardActivationCheckEnter.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(v.getContext(), Class.forName(BaseConstants.LOGIN_PKG_CLASS_NAME));
                if(viewModel.deviceInfoResp == null || viewModel.deviceInfoResp.activateStatus == BaseConstants.DeviceStatus.UNACTIVATED) {
                    intent.putExtra(BaseConstants.EXTRA_REGISTER, true);
                }
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        MaterialCardView cardActivationCheckPrev = view.findViewById(R.id.card_activation_check_prev);
        cardActivationCheckPrev.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        cardActivationCheckPrev.setOnFocusChangeListener(this);

        if(viewModel.deviceInfoResp != null && viewModel.deviceInfoResp.activateStatus == BaseConstants.DeviceStatus.ACTIVATED){
            textActivationCheckEnter.setText("已激活账号登录");
        }else{
            textActivationCheckEnter.setText("注册账号并激活");
        }

        return view;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        MaterialCardView cardView = (v instanceof MaterialCardView) ? (MaterialCardView) v : null;
        if (cardView == null) return;
        final int strokeWidth = DisplayUtil.dip2px(v.getContext(), 1);
        if (hasFocus) {
            cardView.setStrokeColor(getResources().getColor(R.color.selectedStrokeColorBlue));
            cardView.setStrokeWidth(strokeWidth);
            CommonUtils.scaleView(v, 1.1f);
        } else {
            cardView.setStrokeColor(getResources().getColor(R.color.unselectedStrokeColor));
            cardView.setStrokeWidth(0);
            v.clearAnimation();
            CommonUtils.scaleView(v, 1f);
        }
    }
}