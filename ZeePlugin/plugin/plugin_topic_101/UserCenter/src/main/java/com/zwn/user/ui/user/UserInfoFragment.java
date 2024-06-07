package com.zwn.user.ui.user;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.zee.zxing.encode.CodeCreator;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zwn.user.R;
import com.zwn.user.data.protocol.response.UserInfoResp;
import com.zwn.user.ui.UserCenterActivity;
import com.zwn.user.ui.UserCenterViewModel;

public class UserInfoFragment extends Fragment {

    private ConstraintLayout clUserInfoLayout;
    private TextView tvUserName;
    private TextView tvUserCompany;
    private TextView tvUserCompanyAddress;
    private TextView tvUserTel;
    private TextView tvRevise;
    private ImageView invitationCode;
    private UserCenterViewModel mViewModel;
    private LoadingView loadingView;
    private NetworkErrView networkErrView;
    private QrAlertDialog qrAlertDialog;
    private String mSessionId = "";
    private static final Handler mHandle = new Handler(Looper.myLooper());
    private static final long DELAY_MILLIS = 600000;

    public static UserInfoFragment newInstance() {
        return new UserInfoFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(UserCenterViewModel.class);
        initView(view);
        initListener();
        initObService();
        initData();
    }

    private void initView(View view) {
        clUserInfoLayout = view.findViewById(R.id.cl_user_info_content);
        tvUserName = view.findViewById(R.id.tv_user_name_value);
        tvUserTel = view.findViewById(R.id.tv_user_tel_value);
        tvUserCompany = view.findViewById(R.id.tv_user_company_name_value);
        tvUserCompanyAddress = view.findViewById(R.id.tv_user_company_address_value);
        invitationCode = view.findViewById(R.id.iv_user_info_invitation_code);
        tvRevise = view.findViewById(R.id.tv_revise);
        loadingView = view.findViewById(R.id.loading_user_info);
        networkErrView = view.findViewById(R.id.network_user_info);
    }

    private void initData() {
        mViewModel.reqUserInfo();
    }

    private void initObService() {
        mViewModel.userInfoReqState.observe(getViewLifecycleOwner(), loadState -> {
            switch (loadState) {
                case Loading:
                    loadingView.setVisibility(View.VISIBLE);
                    loadingView.startAnim();
                    networkErrView.setVisibility(View.GONE);
                    clUserInfoLayout.setVisibility(View.GONE);
                    break;
                case Success:
                    loadingView.setVisibility(View.GONE);
                    loadingView.stopAnim();
                    networkErrView.setVisibility(View.GONE);
                    clUserInfoLayout.setVisibility(View.VISIBLE);
                    break;
                case Failed:
                    loadingView.setVisibility(View.GONE);
                    loadingView.stopAnim();
                    networkErrView.setVisibility(View.VISIBLE);
                    clUserInfoLayout.setVisibility(View.GONE);
                    break;
            }
        });

        mViewModel.userInfo.observe(getViewLifecycleOwner(), this::updateUserInfo);

        mViewModel.userInfoSessionReqState.observe(getViewLifecycleOwner(), loadState -> {
            if (loadState != null) {
                switch (loadState) {
                    case Loading:
                        loadingView.setVisibility(View.VISIBLE);
                        loadingView.startAnim();
                        break;
                    case Success:
                        loadingView.setVisibility(View.GONE);
                        loadingView.stopAnim();
                        mHandle.postDelayed(() -> mViewModel.createUserInfoSession(), DELAY_MILLIS);
                        break;
                    case Failed:
                        loadingView.setVisibility(View.GONE);
                        loadingView.stopAnim();
                        mViewModel.createUserInfoSession();
                        break;
                }
            }
        });


        mViewModel.userInfoSession.observe(getViewLifecycleOwner(), createSessionForUserInfoResp -> {
            if (createSessionForUserInfoResp != null) {
                if (!createSessionForUserInfoResp.infoUpdatePageUrl.isEmpty() && !createSessionForUserInfoResp.sessionId.isEmpty()) {
                    Bitmap qrCode = CodeCreator.createQRCode(createSessionForUserInfoResp.infoUpdatePageUrl,
                            DisplayUtil.dip2px(requireActivity(), 160),
                            DisplayUtil.dip2px(requireActivity(), 160), null);
                    if (qrCode != null) {
                        showReviseDialog(qrCode);
                        mSessionId = createSessionForUserInfoResp.sessionId;
                        mViewModel.reqUserInfoSessionResult(createSessionForUserInfoResp.sessionId);
                    }
                }
            }
        });

        mViewModel.userInfoSessionResult.observe(getViewLifecycleOwner(), sessionResultStatus -> {
            if (sessionResultStatus != null) {
                switch (sessionResultStatus) {
                    case SUCCESS:
                    case DISABLED:
                        hideReviseDialog();
                        mViewModel.reqUserInfo();
                        break;
                    case TIMEOUT:
                    case FAILED:
                        if (mSessionId.length() > 0) {
                            mViewModel.reqUserInfoSessionResult(mSessionId);
                        }
                        break;
                }
            }
        });

        mViewModel.pToast.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && msg.length() > 0) {
                ((UserCenterActivity) requireActivity()).showToast(msg);
            }
        });
    }

    private void updateUserInfo(@NonNull UserInfoResp userInfoResp) {
        tvUserName.setText(userInfoResp.getUserName());
        tvUserCompany.setText(userInfoResp.getOrganizeName());
        tvUserCompanyAddress.setText(userInfoResp.getSpecificAddress());
        tvUserTel.setText(userInfoResp.getTelephone());

        if (!userInfoResp.getInvitationPageUrl().isEmpty()) {
            invitationCode.setImageBitmap(CodeCreator.createQRCode(userInfoResp.getInvitationPageUrl(), DisplayUtil.dip2px(requireActivity(), 168), DisplayUtil.dip2px(requireActivity(), 168), null));
        }
    }

    private void initListener() {
        tvRevise.setOnClickListener(v -> mViewModel.createUserInfoSession());
        networkErrView.setRetryClickListener(() -> mViewModel.reqUserInfo());
        tvRevise.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                CommonUtils.scaleView(v, 1.1f);
            } else {
                v.clearAnimation();
                CommonUtils.scaleView(v, 1f);
            }
        });
    }

    private void showReviseDialog(@NonNull Bitmap bitmap) {
        final QrAlertDialog qrAlertDialog = new QrAlertDialog(requireActivity());
        this.qrAlertDialog = qrAlertDialog;
        qrAlertDialog.setQrCode(bitmap);
        qrAlertDialog.setOnCancelListener(dialog -> mHandle.removeCallbacksAndMessages(null));
        qrAlertDialog.show();
    }

    private void hideReviseDialog() {
        if (qrAlertDialog != null) {
            qrAlertDialog.cancel();
        }
    }

    @Override
    public void onDestroy() {
        mViewModel.userInfoSession.setValue(null);
        mViewModel.userInfoSessionResult.setValue(null);
        mViewModel.userInfoSessionReqState.setValue(null);
        mViewModel.pToast.setValue(null);
        mHandle.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
