package com.zee.launcher.home.ui.home;

import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.zee.launcher.home.MainViewModel;
import com.zee.launcher.home.R;
import com.zee.launcher.home.data.layout.PageLayoutDTO;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.ui.home.adapter.ClassicListAdapter;
import com.zee.launcher.home.ui.home.holder.ClassicItemViewHolder;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;

import java.util.List;


public class HomeAdvertFragment extends Fragment {

    private MainViewModel mViewModel;
    private RecyclerView recyclerViewHomeAdvert;
    private LoadingView loadingViewHomeClassic;
    private NetworkErrView networkErrViewHomeClassic;
    private LinearLayout llNoData;
    private PageLayoutDTO pageLayoutDTO;
    private int mCategoryIndex = 0;
    private List<ProductListMo.Record> productRecodeLists;
    private ClassicListAdapter adapter;

    public static HomeAdvertFragment newInstance() {
        return new HomeAdvertFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        DensityUtils.autoWidth(requireActivity().getApplication(), requireActivity());
        View view = inflater.inflate(R.layout.fragment_home_advert, container, false);
        initView(view);
        initReqData();
        initListener();
        initViewObserve();


        return view;
    }

    private void initView(View view) {
        recyclerViewHomeAdvert = view.findViewById(R.id.recycler_view_home_advert);
        loadingViewHomeClassic = view.findViewById(R.id.loadingView_home_classic);
        networkErrViewHomeClassic = view.findViewById(R.id.networkErrView_home_classic);
        llNoData = view.findViewById(R.id.ll_no_data);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 5); // 设置列数
        recyclerViewHomeAdvert.setLayoutManager(gridLayoutManager); // 设置布局管理器
        recyclerViewHomeAdvert.addItemDecoration(new GridSpacingItemDecoration(5, 20, false));
        recyclerViewHomeAdvert.setFocusable(true);
        recyclerViewHomeAdvert.setBackground(null);
        recyclerViewHomeAdvert.setDefaultFocusHighlightEnabled(false);
        recyclerViewHomeAdvert.setHasFixedSize(true);

    }

    private void initReqData() {
        mViewModel.reqProductListBySkuIds(mViewModel.getAppSkusByPageIndex(mCategoryIndex), mCategoryIndex + "");
    }

    private void initListener() {
        networkErrViewHomeClassic.setRetryClickListener(() -> initReqData());
    }

    //    @SuppressLint("NotifyDataSetChanged")
    private void initViewObserve() {
        mViewModel.mldProductRecodeListLoadState.observe(getViewLifecycleOwner(), productRecordLoadState -> {
            if (LoadState.Loading == productRecordLoadState.loadState) {
                if (loadingViewHomeClassic.getVisibility() != View.VISIBLE) {
                    loadingViewHomeClassic.setVisibility(View.VISIBLE);
                    loadingViewHomeClassic.startAnim();
                    networkErrViewHomeClassic.setVisibility(View.GONE);
                    recyclerViewHomeAdvert.setVisibility(View.GONE);
                }
            } else if (LoadState.Success == productRecordLoadState.loadState) {
                showData();
            } else {
                loadingViewHomeClassic.stopAnim();
                loadingViewHomeClassic.setVisibility(View.GONE);
                networkErrViewHomeClassic.setVisibility(View.VISIBLE);
                recyclerViewHomeAdvert.setVisibility(View.GONE);
            }
        });


    }

    public void showData() {
        loadingViewHomeClassic.stopAnim();
        loadingViewHomeClassic.setVisibility(View.GONE);
        networkErrViewHomeClassic.setVisibility(View.GONE);
        recyclerViewHomeAdvert.setVisibility(View.VISIBLE);

        productRecodeLists = mViewModel.getProductRecodeListFromCache(mCategoryIndex + "");
        if (productRecodeLists != null && productRecodeLists.size() > 0) {
            if (adapter == null) {
                llNoData.setVisibility(View.GONE);
                adapter = new ClassicListAdapter(productRecodeLists);
                adapter.setOnCreateViewHolder(parent -> {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_type_classic_layout, parent, false);
                    ClassicItemViewHolder classicItemViewHolder = new ClassicItemViewHolder(view);
                    classicItemViewHolder.txtTitle.setVisibility(View.VISIBLE);
                    classicItemViewHolder.txtSummary.setVisibility(View.GONE);

                    return classicItemViewHolder;
                });
                recyclerViewHomeAdvert.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }

        } else {
            llNoData.setVisibility(View.VISIBLE);
        }




    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.mldOnPause.setValue(mCategoryIndex);

    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.mldOnResume.setValue(mCategoryIndex);


    }


}
