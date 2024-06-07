package com.zee.launcher.home.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.MainViewModel;
import com.zee.launcher.home.R;
import com.zee.launcher.home.adapter.CenterLayoutManager;
import com.zee.launcher.home.ui.home.adapter.ProductListAdapter;
import com.zee.launcher.home.ui.home.dialog.ModuleListMenuDialog;
import com.zee.launcher.home.ui.home.model.ModuleMenuMo;
import com.zee.launcher.home.ui.home.model.ProductListType;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeClassicFragment extends Fragment {
    private static final String ARG_CATEGORY_ID = "CategoryId";
    private static final String ARG_CATEGORY_Index = "CategoryIndex";
    private String mCategoryId = "02";
    private int mCategoryIndex;

    private RecyclerView recyclerViewHomeClassic;
    private LoadingView loadingViewHomeClassic;
    private NetworkErrView networkErrViewHomeClassic;
    private ImageView imgToTop;
    private ImageView imgModuleListMenu;
    private LinearLayout llNoData;

    private ProductListAdapter productListAdapter;
    private VerticalItemDecoration verticalItemDecoration;
    private MainViewModel mViewModel;

    private List<ProductListType> typeList;
    private final AtomicInteger pendingCount = new AtomicInteger();
    private CenterLayoutManager centerLayoutManager;

    public HomeClassicFragment() {
    }

    public static HomeClassicFragment newInstance(String categoryId, int categoryIndex) {
        HomeClassicFragment fragment = new HomeClassicFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putInt(ARG_CATEGORY_Index, categoryIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCategoryId = getArguments().getString(ARG_CATEGORY_ID);
            mCategoryIndex = getArguments().getInt(ARG_CATEGORY_Index);
        }
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
        View view = inflater.inflate(R.layout.fragment_home_classic, container, false);

        recyclerViewHomeClassic = view.findViewById(R.id.recycler_view_home_classic);
        loadingViewHomeClassic = view.findViewById(R.id.loadingView_home_classic);
        networkErrViewHomeClassic = view.findViewById(R.id.networkErrView_home_classic);
        imgToTop = view.findViewById(R.id.img_to_top);
        imgModuleListMenu = view.findViewById(R.id.img_module_list_menu);
        llNoData = view.findViewById(R.id.ll_no_data);

        initView();
        initListener();
        initViewObserve();

        if (typeList.size() > 0) {
            initReqData();
        } else {
            llNoData.setVisibility(View.VISIBLE);
        }
        return view;
    }

    private void initView() {
        typeList = mViewModel.getProductListTypeList(mCategoryIndex);
        centerLayoutManager = new CenterLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerViewHomeClassic.setLayoutManager(centerLayoutManager);
    }

    private void initListener() {
        imgToTop.setOnClickListener(v -> {
            recyclerViewHomeClassic.smoothScrollToPosition(0);
        });

        imgModuleListMenu.setOnClickListener(v -> showModuleListMenuDialog(v.getContext()));

        networkErrViewHomeClassic.setRetryClickListener(() -> initReqData());
    }

    private void initReqData() {
        for (int i = 0; i < typeList.size(); i++) {
            ProductListType productListType = typeList.get(i);
            if ((productListType.type == ProductListType.TYPE_BANNER && productListType.swiperLayout.config.appSkus.size() > 0)
                    || productListType.type == ProductListType.TYPE_CLASSIC_MODULE) {
                if (!mViewModel.isExistCacheProductRecodeList(productListType.careKey)) {
                    pendingCount.incrementAndGet();
                    if (productListType.type == ProductListType.TYPE_BANNER) {
                        mViewModel.reqProductListBySkuIds(productListType.swiperLayout.config.appSkus, productListType.careKey);
                    } else {
                        mViewModel.reqProductListBySkuIds(productListType.appCardListLayout.config.appSkus, productListType.careKey);
                    }
                }
            }
        }
        if (pendingCount.get() == 0) {
            recyclerViewHomeClassic.setVisibility(View.VISIBLE);
            productListAdapter = new ProductListAdapter(getViewLifecycleOwner(), mViewModel, typeList, mCategoryIndex);
            productListAdapter.setHasStableIds(true);
            productListAdapter.setOnItemFocusedListener(new ProductListAdapter.OnItemFocusedListener() {
                @Override
                public void onFocused(View v, int position) {
                    centerLayoutManager.smoothScrollToPosition(recyclerViewHomeClassic, new RecyclerView.State(), position);
                }
            });
            recyclerViewHomeClassic.setAdapter(productListAdapter);
            if (verticalItemDecoration == null) {
                verticalItemDecoration = new VerticalItemDecoration(DisplayUtil.dip2px(recyclerViewHomeClassic.getContext(), 2), 0,
                        DisplayUtil.dip2px(recyclerViewHomeClassic.getContext(), 10));
                recyclerViewHomeClassic.addItemDecoration(verticalItemDecoration);
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initViewObserve() {
        mViewModel.mldProductRecodeListLoadState.observe(getViewLifecycleOwner(), productRecordLoadState -> {
            if (productRecordLoadState.careKey.startsWith(mCategoryIndex + "_")) {

                if (LoadState.Loading == productRecordLoadState.loadState) {
                    if (loadingViewHomeClassic.getVisibility() != View.VISIBLE) {
                        loadingViewHomeClassic.setVisibility(View.VISIBLE);
                        loadingViewHomeClassic.startAnim();
                        networkErrViewHomeClassic.setVisibility(View.GONE);
                        recyclerViewHomeClassic.setVisibility(View.GONE);
                    }
                } else if (LoadState.Success == productRecordLoadState.loadState) {
                    checkReqDataDone();
                } else {
                    checkReqDataDone();
                }
            }
        });

        mViewModel.mldNetConnected.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean)
                    productListAdapter.notifyDataSetChanged();
            }
        });
    }

    public void checkReqDataDone() {
        int newPendingCount = pendingCount.decrementAndGet();
        if (newPendingCount <= 0) {

            for (int i = 0; i < typeList.size(); i++) {
                ProductListType productListType = typeList.get(i);
                if ((productListType.type == ProductListType.TYPE_BANNER && productListType.swiperLayout.config.appSkus.size() > 0) || productListType.type == ProductListType.TYPE_CLASSIC_MODULE) {
                    if (!mViewModel.isExistCacheProductRecodeList(productListType.careKey)) {
                        loadingViewHomeClassic.stopAnim();
                        loadingViewHomeClassic.setVisibility(View.GONE);
                        networkErrViewHomeClassic.setVisibility(View.VISIBLE);
                        recyclerViewHomeClassic.setVisibility(View.GONE);
                        return;
                    }
                }
            }

            updateUiByDataReady();
        }
    }

    public void updateUiByDataReady() {
        loadingViewHomeClassic.stopAnim();
        loadingViewHomeClassic.setVisibility(View.GONE);
        networkErrViewHomeClassic.setVisibility(View.GONE);
        recyclerViewHomeClassic.setVisibility(View.VISIBLE);
        productListAdapter = new ProductListAdapter(getViewLifecycleOwner(), mViewModel, typeList, mCategoryIndex);
        productListAdapter.setHasStableIds(true);
        productListAdapter.setOnItemFocusedListener(new ProductListAdapter.OnItemFocusedListener() {
            @Override
            public void onFocused(View v, int position) {
                centerLayoutManager.smoothScrollToPosition(recyclerViewHomeClassic, new RecyclerView.State(), position);
            }
        });
        recyclerViewHomeClassic.setAdapter(productListAdapter);
        if (verticalItemDecoration == null) {
            verticalItemDecoration = new VerticalItemDecoration(DisplayUtil.dip2px(recyclerViewHomeClassic.getContext(), 2), 0,
                    DisplayUtil.dip2px(recyclerViewHomeClassic.getContext(), 18));
            recyclerViewHomeClassic.addItemDecoration(verticalItemDecoration);
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

    private void showModuleListMenuDialog(Context context) {
        ModuleListMenuDialog moduleListDialog = new ModuleListMenuDialog(context);
        List<ModuleMenuMo> dataList = new ArrayList<>();
        for (int i = 0; i < typeList.size(); i++) {
            ProductListType productListType = typeList.get(i);
            if (productListType.listTitle == null || productListType.listTitle.isEmpty()) {
                continue;
            }
            dataList.add(new ModuleMenuMo(productListType.listTitle));
        }
        moduleListDialog.setDataList(dataList);
        moduleListDialog.setOnMenuSelectListener(position -> {
            moduleListDialog.dismiss();
            recyclerViewHomeClassic.smoothScrollToPosition(position);
        });
        moduleListDialog.show();
    }
}