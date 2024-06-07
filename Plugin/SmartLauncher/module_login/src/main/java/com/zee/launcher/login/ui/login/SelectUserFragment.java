package com.zee.launcher.login.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zee.launcher.login.R;
import com.zee.launcher.login.data.protocol.response.UserInfoResp;
import com.zee.launcher.login.ui.LoginViewModel;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;

import java.util.List;


public class SelectUserFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener {

    private RecyclerView recyclerViewUserBindSelect;
    private MaterialCardView cardUserBindPrev;
    private LoginViewModel mViewModel;
    private UserInfoListAdapter userInfoListAdapter;

    public static SelectUserFragment newInstance() {
        return new SelectUserFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        return inflater.inflate(R.layout.fragment_select_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initListener(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        List<UserInfoResp> userOptionInfoList = mViewModel.mldUserOptionInfoListSelect.getValue();
        if (userInfoListAdapter == null) {
            userInfoListAdapter = new UserInfoListAdapter(userOptionInfoList);
            userInfoListAdapter.setOnItemClickListener((view, userOptionInfo) -> handleUserSelected(userOptionInfo));
            recyclerViewUserBindSelect.setAdapter(userInfoListAdapter);
        } else {
            userInfoListAdapter.updateDataList(userOptionInfoList);
        }
    }

    private void initView(View view) {
        recyclerViewUserBindSelect = view.findViewById(R.id.recycler_view_user_info);
        cardUserBindPrev = view.findViewById(R.id.card_user_bind_select_prev);
    }

    private void initListener(View view) {
        cardUserBindPrev.setOnClickListener(this);
        cardUserBindPrev.setOnFocusChangeListener(this);
    }

    private void handleUserSelected(UserInfoResp userInfoResp) {
        if (mViewModel.isDeviceActivated) {
            mViewModel.userPwdLoginReq.selectUserCode = userInfoResp.userCode;
            mViewModel.reqUserPwdLogin(mViewModel.userPwdLoginReq);
        } else {
            if (mViewModel.userRegisterBindingReq != null) {
                mViewModel.userRegisterBindingReq.selectUserCode = userInfoResp.userCode;
                mViewModel.reqUserRegisterBinding(mViewModel.userRegisterBindingReq);
            } else {
                mViewModel.userLoginBindingReq.selectUserCode = userInfoResp.userCode;
                mViewModel.reqUserLoginBinding(mViewModel.userLoginBindingReq);
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (userInfoListAdapter != null) {
            userInfoListAdapter.setOnItemClickListener(null);
            userInfoListAdapter = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.card_user_bind_select_prev) {
            mViewModel.mldUserSelectBack.setValue(true);
        }
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
