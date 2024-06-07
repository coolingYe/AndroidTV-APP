package com.zee.launcher.home.ui.product.rule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zee.launcher.home.R;
import com.zee.launcher.home.adapter.ProductListAdapter;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.paged.HorizontalRecyclerView;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.widgets.CenterGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class RuleFragment extends Fragment {

    private ProductListAdapter productListAdapter;
    private HorizontalRecyclerView recyclerView;

    public static RuleFragment newInstance() {
        return new RuleFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initListener();
    }

    private void initView(View view) {
        View backView = view.findViewById(R.id.view_rule_back);
        backView.setOnClickListener(v -> requireActivity().finish());
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
        recyclerView = view.findViewById(R.id.rv_rule_category);
        CenterGridLayoutManager centerLayoutManager = new CenterGridLayoutManager(requireActivity(), 3);
        recyclerView.setLayoutManager(centerLayoutManager);
        List<ProductListMo.Record> records = new ArrayList<ProductListMo.Record>() {{
            add(new ProductListMo.Record(getString(R.string.activity_description), R.mipmap.ic_activity_description));
            add(new ProductListMo.Record(getString(R.string.activity_rules), R.mipmap.ic_activity_rule));
            add(new ProductListMo.Record(getString(R.string.leaderboard), R.mipmap.ic_leaderborad));
        }};
        productListAdapter = new ProductListAdapter(records, ProductListAdapter.TYPE_PERSON_TWO);
        recyclerView.setAdapter(productListAdapter);
        recyclerView.post(recyclerView::requestFocus);
    }

    private void initListener() {
        productListAdapter.setOnItemFocusedListens(integer -> {
            switch (integer) {
                case 0:
                    navigationToFragment(DescriptionFragment.newInstance(DescriptionFragment.TYPE_FROM_DESCRIPTION));
                    break;
                case 1:
                    navigationToFragment(DescriptionFragment.newInstance(DescriptionFragment.TYPE_FROM_RULE));
                    break;
                case 2:
                    navigationToFragment(LeaderboardFragment.newInstance());
                    break;
            }
        });
    }

    private void navigationToFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fl_rule, fragment).addToBackStack(null).commit();
    }
}
