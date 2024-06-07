package com.zee.launcher.home.ui.product.rule;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;
import com.zee.launcher.home.adapter.TopLayoutManager;
import com.zee.launcher.home.data.protocol.response.RankingResp;
import com.zee.launcher.home.ui.product.adapter.RankingListAdapter;
import com.zee.launcher.home.widgets.VerticalRecyclerView;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;

import java.util.ArrayList;

public class LeaderboardFragment extends Fragment {

    private RuleViewModel mViewModel;
    private VerticalRecyclerView recyclerView;
    private View selfTopView;
    private TextView tvSelfNum, tvSelfLose, tvSelfResult, tvSelfName;
    private RankingListAdapter rankingListAdapter;
    private TopLayoutManager topLayoutManager;
    private int targetIndex = 0;
    private LoadingView loadingView;
    private NetworkErrView networkErrView;
    private ConstraintLayout clLeaderboardLayout;
    private LinearLayout llLeaderBoardFrame;

    public static LeaderboardFragment newInstance() {
        return new LeaderboardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(RuleViewModel.class);
        initView(view);
        initListener();
        initObService(view);
        mViewModel.getRanking();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView(View view) {
        View backView = view.findViewById(R.id.view_leaderboard_back);
        backView.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        backView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (v.getId() == backView.getId()) {
                    backView.setBackgroundResource(com.zwn.user.R.mipmap.ic_back_selected_bg);
                    CommonUtils.scaleView(v, 1.1f);
                }
            } else {
                if (v.getId() == backView.getId()) {
                    backView.setBackgroundResource(com.zwn.user.R.mipmap.ic_back_bg);
                    v.clearAnimation();
                    CommonUtils.scaleView(v, 1f);
                }
            }
        });

        clLeaderboardLayout = view.findViewById(R.id.view_leaderboard_layout);
        topLayoutManager = new TopLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView = view.findViewById(R.id.rv_ranking);
        llLeaderBoardFrame = view.findViewById(R.id.ll_leaderboard_frame);
        recyclerView.setLayoutManager(topLayoutManager);
        selfTopView = view.findViewById(R.id.self_view);
        tvSelfNum = selfTopView.findViewById(R.id.tv_top_number);
        tvSelfLose = selfTopView.findViewById(R.id.tv_self_title);
        tvSelfName = selfTopView.findViewById(R.id.tv_company_name);
        tvSelfResult = selfTopView.findViewById(R.id.tv_result_value);
        selfTopView.setFocusable(false);
        loadingView = view.findViewById(R.id.loadingView_leaderboard_classic);
        networkErrView = view.findViewById(R.id.networkErrView_leaderboard_classic);

        rankingListAdapter = new RankingListAdapter(new ArrayList<>());
        recyclerView.setAdapter(rankingListAdapter);
        recyclerView.setNextFocusUpId(recyclerView.getId());
        recyclerView.requestFocus();
        startAnim(view);
    }

    private void startAnim(View view) {
        ImageView clPicLayout = view.findViewById(R.id.iv_pic_layout);
        ConstraintLayout clRankingLayout = view.findViewById(R.id.cl_ranking_layout);
        Animation animationLeft = AnimationUtils.loadAnimation(requireActivity(), R.anim.enter_left);
        Animation animationRight = AnimationUtils.loadAnimation(requireActivity(), R.anim.enter_right);
        clPicLayout.startAnimation(animationLeft);
        clRankingLayout.startAnimation(animationRight);
    }

    private void initListener() {
        recyclerView.setOnHandleKeyEventListener(event -> {
            loadMore(event);
            return false;
        });
        recyclerView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                llLeaderBoardFrame.setBackgroundResource(R.mipmap.ic_leaderboard_selected_frame);
            } else {
                llLeaderBoardFrame.setBackgroundResource(R.drawable.bg_default);
            }
        });
        networkErrView.setRetryClickListener(() -> mViewModel.getRanking());
    }

    private void loadMore(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (targetIndex - 5 >= 0) {
                        targetIndex -= 5;
                        topLayoutManager.smoothScrollToPosition(recyclerView, new RecyclerView.State(), targetIndex);
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (targetIndex + 5 <= rankingListAdapter.getItemCount()) {
                        targetIndex += 5;
                        topLayoutManager.smoothScrollToPosition(recyclerView, new RecyclerView.State(), targetIndex);
                    }
                    break;
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private void initObService(View view) {
        mViewModel.mldInitDataLoadState.observe(getViewLifecycleOwner(), loadState -> {
            switch (loadState) {
                case Loading:
                    loadingView.setVisibility(View.VISIBLE);
                    networkErrView.setVisibility(View.GONE);
                    clLeaderboardLayout.setVisibility(View.GONE);
                    loadingView.startAnim();
                    break;
                case Success:
                    loadingView.stopAnim();
                    loadingView.setVisibility(View.GONE);
                    networkErrView.setVisibility(View.GONE);
                    clLeaderboardLayout.setVisibility(View.VISIBLE);
                    startAnim(view);
                    break;
                case Failed:
                    loadingView.stopAnim();
                    loadingView.setVisibility(View.GONE);
                    clLeaderboardLayout.setVisibility(View.GONE);
                    networkErrView.setVisibility(View.VISIBLE);
                    break;
            }
        });
        mViewModel.rankingResp.observe(getViewLifecycleOwner(), rankingResp -> {
            recyclerView.post(() -> {
                rankingListAdapter.updateList(rankingResp.getRankingTopN());
                updateSelfView(rankingResp.getSelf());
                recyclerView.requestFocus();
            });
        });
        mViewModel.userInfoReqState.observe(getViewLifecycleOwner(), loadState -> {
            if (loadState == LoadState.Success) {
                if (mViewModel.userOrganizeName != null && mViewModel.userOrganizeName.length() > 0) {
                    selfTopView.setVisibility(View.VISIBLE);
                    selfTopView.setBackgroundResource(R.mipmap.ic_ranking_item_bg);
                    tvSelfNum.setVisibility(View.GONE);
                    tvSelfLose.setVisibility(View.VISIBLE);
                    if (mViewModel.userOrganizeName.length() > 11) {
                        tvSelfName.setText(mViewModel.userOrganizeName.substring(0, 11) + "...");
                    } else tvSelfName.setText(mViewModel.userOrganizeName);
                    tvSelfResult.setText("0");
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateSelfView(RankingResp.Self self) {
        if (self != null) {
            selfTopView.setVisibility(View.VISIBLE);
            selfTopView.setBackgroundResource(R.mipmap.ic_ranking_item_bg);
            tvSelfNum.setVisibility(View.VISIBLE);
            tvSelfNum.setText(String.valueOf(self.getRankingNum()));
            tvSelfLose.setVisibility(View.GONE);
            if (self.getUserName() != null ) {
                if (self.getUserName().length() > 11) {
                    tvSelfName.setText(self.getUserName().substring(0, 11) + "...");
                } else tvSelfName.setText(self.getUserName());
            }
            tvSelfResult.setText(String.valueOf(self.getPlayScore()));
        } else {
            mViewModel.reqUserInfo();
        }
    }

}
