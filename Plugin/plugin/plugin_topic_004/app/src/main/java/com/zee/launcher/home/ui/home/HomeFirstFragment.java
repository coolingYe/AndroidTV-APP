package com.zee.launcher.home.ui.home;

import static android.view.View.inflate;
import static com.zee.launcher.home.utils.GlobalLayoutHelper.filterAPP;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.MainActivity;
import com.zee.launcher.home.MainViewModel;
import com.zee.launcher.home.R;
import com.zee.launcher.home.adapter.CenterLayoutManager;
import com.zee.launcher.home.data.layout.PageLayoutDTO;
import com.zee.launcher.home.data.layout.SwiperLayout;
import com.zee.launcher.home.ui.home.adapter.ProductListAdapter;
import com.zee.launcher.home.ui.home.model.Category;
import com.zee.launcher.home.ui.home.model.ProductListType;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.FontUtils;
import com.zeewain.base.utils.GlideApp;
import com.zeewain.base.widgets.CustomerFlowLayout;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;

import java.util.ArrayList;
import java.util.List;

public class HomeFirstFragment extends Fragment {
    private static final String ARG_CATEGORY_ID = "CategoryId";
    private static final String ARG_CATEGORY_Index = "CategoryIndex";
    private int mCategoryIndex;

    private List<ProductListType> typeList;
    private PageLayoutDTO pageLayoutDTO;

    private MainViewModel mViewModel;
    private RecyclerView recyclerViewHomeClassic;
    private LoadingView loadingViewHomeClassic;
    private NetworkErrView networkErrViewHomeClassic;
    private LinearLayout llNoData;
    private CenterLayoutManager centerLayoutManager;
    private VerticalItemDecoration verticalItemDecoration;
    private ProductListAdapter productListAdapter;
    private CustomerFlowLayout customerFlowLayout;
    private boolean hasBannerAPP = false;
    private MainActivity mainActivity;

    public static HomeFirstFragment newInstance(String categoryId, int categoryIndex) {
        HomeFirstFragment fragment = new HomeFirstFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putInt(ARG_CATEGORY_Index, categoryIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCategoryIndex = getArguments().getInt(ARG_CATEGORY_Index);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        DensityUtils.autoWidth(requireActivity().getApplication(), requireActivity());
        View view = inflater.inflate(R.layout.fragment_home_first, container, false);

        recyclerViewHomeClassic = view.findViewById(R.id.recycler_view_home_classic);
        customerFlowLayout = view.findViewById(R.id.customer_view);
        loadingViewHomeClassic = view.findViewById(R.id.loadingView_home_classic);
        networkErrViewHomeClassic = view.findViewById(R.id.networkErrView_home_classic);
        llNoData = view.findViewById(R.id.ll_no_data);
        TextView tvNoData = view.findViewById(R.id.tv_no_data);
        tvNoData.setTypeface(FontUtils.typefaceRegular);

        initView();
        initObserve();

        if (typeList.size() > 0) {
            initReqData();
        } else llNoData.setVisibility(View.VISIBLE);
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initObserve() {
        mViewModel.mldProductRecodeListLoadState.observe(getViewLifecycleOwner(), productRecordListLoadState -> {
            if (productRecordListLoadState.careKey.startsWith(mCategoryIndex + "_")) {
                if (productRecordListLoadState.loadState == LoadState.Success) {
                    productListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void initReqData() {
        for (int i = 0; i < typeList.size(); i++) {
            ProductListType productListType = typeList.get(i);
            if (productListType.type == ProductListType.TYPE_BANNER) {
                if (!mViewModel.isExistCacheProductRecodeList(productListType.careKey)) {
                    if (productListType.type == ProductListType.TYPE_BANNER) {
                        if (productListType.swiperLayout.uid.equals("mixed-swiper")) {
                            for (SwiperLayout.Item items : productListType.swiperLayout.config.items) {
                                if ("app".equals(items.kind)) {
                                    hasBannerAPP = true;
                                    break;
                                }
                            }
                            if (hasBannerAPP) {
                                List<String> appSkus = filterAPP(productListType.swiperLayout.config.items);
                                if (appSkus.size() > 0) {
                                    mViewModel.reqProductListBySkuIds(appSkus, productListType.careKey);
                                }
                            }
                        } else {
                            mViewModel.reqProductListBySkuIds(productListType.swiperLayout.config.appSkus, productListType.careKey);
                        }
                    }
                }
            }
        }
    }

    private void initView() {
        typeList = new ArrayList<>();
        pageLayoutDTO = mViewModel.globalLayout.layout.pages.get(mCategoryIndex);

        if (pageLayoutDTO.swiperLayout != null) {
            ProductListType productListType = new ProductListType(ProductListType.TYPE_BANNER, mCategoryIndex + "_banner", "banner");
            productListType.swiperLayout = pageLayoutDTO.swiperLayout;
            if (pageLayoutDTO.swiperLayout.uid.equals("mixed-swiper")) {
                if (productListType.swiperLayout.config.items.size() > 0)
                    typeList.add(productListType);
            } else {
                if (productListType.swiperLayout.config.appSkus.size() > 0)
                    typeList.add(productListType);
            }
        }

        centerLayoutManager = new CenterLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerViewHomeClassic.setLayoutManager(centerLayoutManager);
        recyclerViewHomeClassic.setVisibility(View.VISIBLE);
        productListAdapter = new ProductListAdapter(getViewLifecycleOwner(), mViewModel, typeList, mCategoryIndex);
        productListAdapter.setHasStableIds(true);
        productListAdapter.setOnItemFocusedListener((v, position) -> centerLayoutManager.smoothScrollToPosition(recyclerViewHomeClassic, new RecyclerView.State(), position));
        recyclerViewHomeClassic.setAdapter(productListAdapter);
        if (verticalItemDecoration == null) {
            verticalItemDecoration = new VerticalItemDecoration(DisplayUtil.dip2px(recyclerViewHomeClassic.getContext(), 2), 0,
                    DisplayUtil.dip2px(recyclerViewHomeClassic.getContext(), 10));
            recyclerViewHomeClassic.addItemDecoration(verticalItemDecoration);
        }

        initPageCard();
    }

    private void initPageCard() {
        customerFlowLayout.removeAllViews();
        List<Category> pageInfoList = getCategories(mViewModel.globalLayout.layout.pages);
        if (pageInfoList.size() > 1) {
            for (final Category category : pageInfoList) {
                if (category.getCategoryName().equals("首页")) continue;
                View view = inflate(getContext(), com.zeewain.base.R.layout.item_home_first_page_card_layout, null);
                if (category.getCategoryIndex() == 1) {
                    view.setOnKeyListener((v, keyCode, event) -> {
                        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                            v.clearAnimation();
                            v.startAnimation(AnimationUtils.loadAnimation(getContext(), com.zeewain.base.R.anim.host_shake));
                            return true;
                        }
                        return false;
                    });
                } else if (category.getCategoryIndex() % 6 == 0) {
                    view.setNextFocusUpId(R.id.cl_banner_root);
                    view.setOnKeyListener((v, keyCode, event) -> {
                        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                            v.clearAnimation();
                            v.startAnimation(AnimationUtils.loadAnimation(getContext(), com.zeewain.base.R.anim.host_shake));
                            return true;
                        }
                        return false;
                    });
                }
                ImageView imageView = view.findViewById(com.zeewain.base.R.id.iv_home_first_page_card_mask);
                GlideApp.with(imageView).load(category.getCategoryImage()).into(imageView);
                view.setTag(category);
                view.setOnClickListener(v -> ((MainActivity) requireActivity()).shiftPages(category.getCategoryIndex()));
                view.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        CommonUtils.scaleView(v, 1.05f);
                    } else {

                        CommonUtils.scaleView(v, 1f);
                    }
                });

                customerFlowLayout.addView(view);
            }
        }
    }

    private List<Category> getCategories(List<PageLayoutDTO> pageInfoList) {
        List<Category> categoryList = new ArrayList<>();
        for (int i = 0; i < pageInfoList.size(); i++) {
            PageLayoutDTO currentInfo = pageInfoList.get(i);
            categoryList.add(new Category(currentInfo.name, currentInfo.config.fullName, currentInfo.config.preview, i));
        }
        return categoryList;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }
}
