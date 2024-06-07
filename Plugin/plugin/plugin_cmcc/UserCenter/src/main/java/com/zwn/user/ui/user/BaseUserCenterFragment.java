package com.zwn.user.ui.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.FontUtils;
import com.zeewain.base.widgets.CenterGridLayoutManager;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zwn.user.R;
import com.zwn.user.adapter.UserCommonAdapter;
import com.zwn.user.data.model.UserPageCommonItem;
import com.zwn.user.ui.UserCenterViewModel;

public abstract class BaseUserCenterFragment extends Fragment implements View.OnFocusChangeListener{
    protected static final int DETAIL_BACK_CODE = 1001;
    public MaterialCardView cardUserCommPageDelAll;
    public MaterialCardView cardUserCommPageDel;
    public MaterialCardView cardUserCommPageCache;
    public ImageView ivUserCommPageDel, ivUserCommPageDelAll;
    public NetworkErrView nevUserComm;
    public ImageView ivUserCommSaturn;
    public TextView tvUserCommEmpty;
    public LoadingView lvUserCommLoading;
    public TextView tvUserCommonTab;
    protected UserCenterViewModel mViewModel;
    public boolean mDelMode = false;
    protected UserCommonAdapter mAdapter;
    public RecyclerView rvUserCommPage;
    public CenterGridLayoutManager centerGridLayoutManager;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_common_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(UserCenterViewModel.class);
        initView(view);
        initChannel();
        initListener();
    }

    public void initView(View view) {
        cardUserCommPageDelAll = view.findViewById(R.id.card_user_comm_page_del_all);
        cardUserCommPageDel = view.findViewById(R.id.card_user_comm_page_del);
        cardUserCommPageCache = view.findViewById(R.id.card_user_comm_page_cache);
        ivUserCommPageDel = view.findViewById(R.id.iv_user_comm_page_del);
        ivUserCommPageDelAll = view.findViewById(R.id.iv_user_comm_page_del_all);
        nevUserComm = view.findViewById(R.id.nev_user_comm);
        ivUserCommSaturn = view.findViewById(R.id.iv_user_comm_saturn);
        tvUserCommEmpty = view.findViewById(R.id.tv_user_comm_empty);
        lvUserCommLoading = view.findViewById(R.id.lv_user_comm_loading);
        tvUserCommonTab = view.findViewById(R.id.tv_user_common_tab);

        cardUserCommPageDelAll.setVisibility(View.GONE);
        cardUserCommPageDel.setVisibility(View.GONE);
        nevUserComm.setVisibility(View.INVISIBLE);
        ivUserCommSaturn.setVisibility(View.INVISIBLE);
        tvUserCommEmpty.setVisibility(View.INVISIBLE);
        lvUserCommLoading.startAnim();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initChannel() {
        tvUserCommonTab.setTextColor(requireActivity().getColor(R.color.user_center_tab_title_color));
        tvUserCommonTab.setPaddingRelative(0,0,0, DisplayUtil.dip2px(requireContext(),6));
        tvUserCommonTab.setBackgroundResource(R.drawable.shape_user_center_tab_bottom_line_bg);
        ivUserCommPageDel.setImageResource(R.mipmap.icon_delete_btn);
        ivUserCommPageDelAll.setImageResource(R.mipmap.icon_delete_all);
        tvUserCommEmpty.setTypeface(FontUtils.typefaceRegular);
        if (CommonUtils.isTopic003Enable()) {
            tvUserCommonTab.setTypeface(FontUtils.typefaceMedium);
        }
    }

    public void initListener() {
        cardUserCommPageDelAll.setOnFocusChangeListener(this);
        cardUserCommPageDel.setOnFocusChangeListener(this);
        cardUserCommPageDel.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.host_shake));
            }
            return false;
        });
    }


    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        MaterialCardView cardView = (view instanceof MaterialCardView) ? (MaterialCardView) view : null;
        if (cardView == null) return;
        if (hasFocus) {
            cardView.setStrokeColor(requireActivity().getColor(R.color.all_focused_frame_color));
            cardView.setStrokeWidth(DisplayUtil.dip2px(requireActivity(), 2f));
            CommonUtils.scaleView(view, 1.1f);
        } else {
            cardView.setStrokeColor(0x00FFFFFF);
            cardView.setStrokeWidth(0);
            view.clearAnimation();
            CommonUtils.scaleView(view, 1f);
        }
    }

    protected void initOtherListener() {
        cardUserCommPageDel.setOnClickListener(v -> {
            mDelMode = !mDelMode;
            mAdapter.setDelMode(mDelMode);
            ivUserCommPageDel.setImageResource(mDelMode ? R.mipmap.icon_delete_btn_selected : R.mipmap.icon_delete_btn);
            cardUserCommPageDelAll.setVisibility(mDelMode ? View.VISIBLE : View.GONE);
        });

        mAdapter.setOnItemClickListener((view, position, commonItem) -> {
            if (position == RecyclerView.NO_POSITION || mDelMode) {
                return;
            }
            if(mAdapter.getItemCount() > position) {
                UserPageCommonItem item = (UserPageCommonItem) mAdapter.getItem(position);
                try {
                    Intent intent = new Intent(getActivity(), Class.forName("com.zee.launcher.home.ui.detail.DetailActivity"));
                    intent.putExtra("skuId", item.skuId);
                    startActivityForResult(intent, DETAIL_BACK_CODE);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void checkStateMine() {
        if (mAdapter.getItemCount() == 0) {
            mDelMode = true;
            cardUserCommPageDel.performClick();
            cardUserCommPageDel.setVisibility(View.GONE);
            ivUserCommSaturn.setVisibility(View.VISIBLE);
            tvUserCommEmpty.setVisibility(View.VISIBLE);
            rvUserCommPage.setVisibility(View.INVISIBLE);
            rvUserCommPage.setVisibility(View.INVISIBLE);
        } else {
            ivUserCommSaturn.setVisibility(View.INVISIBLE);
            tvUserCommEmpty.setVisibility(View.INVISIBLE);
            cardUserCommPageDel.setVisibility(View.VISIBLE);
            rvUserCommPage.setVisibility(View.VISIBLE);
            rvUserCommPage.setVisibility(View.VISIBLE);
        }
        tvUserCommonTab.setText(String.format("全部(%d)", mAdapter.getItemCount()));
    }

    public void backToNormalMode(){
        mDelMode = false;
        mAdapter.setDelMode(false);
        ivUserCommPageDel.setImageResource(mDelMode ? R.mipmap.icon_delete_btn_selected : R.mipmap.icon_delete_btn);
        cardUserCommPageDelAll.setVisibility(mDelMode ? View.VISIBLE : View.GONE);
    }
}
