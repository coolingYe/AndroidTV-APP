package com.zee.launcher.home.ui.home.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Outline;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.MainViewModel;
import com.zee.launcher.home.R;
import com.zee.launcher.home.config.LayoutConf;
import com.zee.launcher.home.data.layout.AppCardListLayout;
import com.zee.launcher.home.data.layout.SwiperLayout;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.ui.home.HorizontalItemDecoration;
import com.zee.launcher.home.ui.home.holder.ClassicItemViewHolder;
import com.zee.launcher.home.ui.home.model.ProductListType;
import com.zee.launcher.home.widgets.BannerConstraintLayout;
import com.zee.launcher.home.widgets.banner.Banner;
import com.zee.launcher.home.widgets.banner.OnPageChangeListener;
import com.zee.paged.HorizontalRecyclerView;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.FontUtils;
import com.zeewain.base.widgets.CenterGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final LifecycleOwner lifecycleOwner;
    private final MainViewModel viewModel;
    private final List<ProductListType> typeList;
    private final int index;

    public ProductListAdapter(LifecycleOwner lifecycleOwner, MainViewModel viewModel, List<ProductListType> typeList, int index) {
        this.lifecycleOwner = lifecycleOwner;
        this.viewModel = viewModel;
        this.typeList = typeList;
        this.index = index;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ProductListType.TYPE_BANNER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_type_banner, parent, false);
            SwiperLayout swiperLayout = viewModel.globalLayout.layout.pages.get(index).swiperLayout;
            if (swiperLayout.uid.equals("mixed-swiper")) {
                return new BannerViewHolder(view, LayoutConf.BannerType.PAGED);
            }
            if ("right-side".equals(swiperLayout.config.displayMode)) {
                return new BannerViewHolder(view, LayoutConf.BannerType.RIGHT_SIDE);
            } else if ("page".equals(swiperLayout.config.displayMode)) {
                return new BannerViewHolder(view, LayoutConf.BannerType.PAGED);
            } else {
                return new BannerViewHolder(view, LayoutConf.BannerType.GALLERY);
            }
        } else /*if(viewType == ProductListType.TYPE_CLASSIC_MODULE)*/ {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_type_recycler_view, parent, false);
            return new ClassicModuleViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BannerViewHolder) {
            ((BannerViewHolder) holder).bind(typeList.get(position));
        } else if (holder instanceof ClassicModuleViewHolder) {
            ((ClassicModuleViewHolder) holder).bind(typeList.get(position), position);
        } else if (holder instanceof FooterViewHolder) {
            ((FooterViewHolder) holder).bind();
        }
    }

    @Override
    public int getItemCount() {
        return typeList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return typeList.get(position).type;
    }

    class BannerViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout clBannerRoot;
        private final BannerConstraintLayout clBannerChild;
        private final Banner bannerHome;
        private final RecyclerView recyclerViewBannerIndex;
        private final List<ProductListMo.Record> bannerDataList = new ArrayList<>(5);
        private final ClassicBannerIndexAdapter bannerIndexAdapter;
        private ClassicBannerAdapter classicBannerAdapter;
        private final int bannerType;

        @SuppressLint("NotifyDataSetChanged")
        public void bind(ProductListType productListType) {
            bannerDataList.clear();

            if (productListType.swiperLayout.uid.equals("mixed-swiper")) {
                bannerDataList.addAll(filterImage(productListType.swiperLayout.config.items));
            }
            List<ProductListMo.Record> productRecordListMo = viewModel.getProductRecodeListFromCache(productListType.careKey);
            if (productRecordListMo != null) {
                List<ProductListMo.Record> dataListProd = productRecordListMo;
                dataListProd.forEach(record -> record.setKind("app"));
                if (productRecordListMo.size() > 5) {
                    dataListProd = dataListProd.subList(0, 5);
                }
                bannerDataList.addAll(dataListProd);
            }
            if (productListType.swiperLayout.uid.equals("mixed-swiper")) {
                bannerDataList.addAll(filterVideo(productListType.swiperLayout.config.items));
            }

            //binding.imgBannerShadowMask.setVisibility(View.VISIBLE);
            recyclerViewBannerIndex.setVisibility(View.VISIBLE);

            classicBannerAdapter = new ClassicBannerAdapter(bannerDataList);
            classicBannerAdapter.setHasStableIds(true);
            classicBannerAdapter.setShowTitleSummary(!(bannerType == LayoutConf.BannerType.RIGHT_SIDE));
            bannerHome.setAdapter(classicBannerAdapter);

            bannerIndexAdapter.updateDataList(bannerDataList);
            bannerHome.setCurrentItem(bannerIndexAdapter.getSelectIndex() + 1, false);
            clBannerChild.setBackgroundResource(R.drawable.default_bg);
        }

        private List<ProductListMo.Record> filterImage(List<SwiperLayout.Item> mixedBanners) {
            List<ProductListMo.Record> banners = new ArrayList<>();
            mixedBanners.forEach(item -> {
                if (item.kind.equals("img")) {
                    banners.add(new ProductListMo.Record(item.kind, item.url));
                }
            });
            return banners;
        }

        private List<ProductListMo.Record> filterVideo(List<SwiperLayout.Item> mixedBanners) {
            List<ProductListMo.Record> banners = new ArrayList<>();
            mixedBanners.forEach(item -> {
                if (item.kind.equals("video")) {
                    banners.add(new ProductListMo.Record(item.kind, item.url));
                }
            });
            return banners;
        }

        @SuppressLint("SuspiciousIndentation")
        public BannerViewHolder(@NonNull View view, int bannerType) {
            super(view);
            this.bannerType = bannerType;
            clBannerRoot = view.findViewById(R.id.cl_banner_root);
            clBannerChild = view.findViewById(R.id.cl_banner_child);
            clBannerRoot.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        if (bannerHome.hasVideoItem() && bannerHome.hasVideoPlaying()) {
                            bannerHome.stop();
                            bannerHome.setAutoLoop(false);
                        } else bannerHome.startAutoLoop(true);
                        clBannerRoot.clearAnimation();
                        CommonUtils.scaleView(clBannerRoot, 1f);
                        clBannerRoot.setBackgroundColor(Color.TRANSPARENT);
                    } else {
                        bannerHome.stop();
                        bannerHome.setAutoLoop(false);
                        CommonUtils.scaleView(clBannerRoot, 1.02f);
                        clBannerRoot.setBackgroundResource(R.drawable.shape_home_banner_focused_frame);
                    }
                }
            });
            clBannerRoot.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    return bannerHome.dispatchKeyEvent(event);
                }
            });

            bannerHome = view.findViewById(R.id.banner_home);
            recyclerViewBannerIndex = view.findViewById(R.id.recycler_view_banner_index);
            recyclerViewBannerIndex.setFocusable(false);

            if (bannerType == LayoutConf.BannerType.RIGHT_SIDE) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(clBannerChild);
                constraintSet.clear(R.id.recycler_view_banner_index);
                constraintSet.connect(R.id.recycler_view_banner_index, ConstraintSet.RIGHT, R.id.cl_banner_root, ConstraintSet.RIGHT);
                constraintSet.connect(R.id.recycler_view_banner_index, ConstraintSet.BOTTOM, R.id.cl_banner_root, ConstraintSet.BOTTOM);
                constraintSet.connect(R.id.recycler_view_banner_index, ConstraintSet.TOP, R.id.cl_banner_root, ConstraintSet.TOP);
                constraintSet.constrainWidth(R.id.recycler_view_banner_index, DisplayUtil.dip2px(view.getContext(), 270));
                constraintSet.applyTo(clBannerChild);
            } else {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(clBannerChild);
                constraintSet.clear(R.id.recycler_view_banner_index);
                constraintSet.connect(R.id.recycler_view_banner_index, ConstraintSet.RIGHT, R.id.cl_banner_root, ConstraintSet.RIGHT, DisplayUtil.dip2px(view.getContext(), 36));
                constraintSet.connect(R.id.recycler_view_banner_index, ConstraintSet.BOTTOM, R.id.cl_banner_root, ConstraintSet.BOTTOM, DisplayUtil.dip2px(view.getContext(), 15));
                constraintSet.constrainHeight(R.id.recycler_view_banner_index, DisplayUtil.dip2px(view.getContext(), 10));
                constraintSet.applyTo(clBannerChild);

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                recyclerViewBannerIndex.setLayoutManager(linearLayoutManager);
                recyclerViewBannerIndex.setBackgroundColor(0x00000000);
                recyclerViewBannerIndex.addItemDecoration(new HorizontalItemDecoration(DisplayUtil.dip2px(recyclerViewBannerIndex.getContext(), 13), 0));

                if (bannerType == LayoutConf.BannerType.GALLERY) {
                    bannerHome.setBannerGalleryEffect(DisplayUtil.dip2px(view.getContext(), 200),
                            DisplayUtil.dip2px(view.getContext(), 200),
                            DisplayUtil.dip2px(view.getContext(), 8), 1f);
                }
            }

            clBannerChild.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), DisplayUtil.dip2px(view.getContext(), 5));
                }
            });
            clBannerChild.setClipToOutline(true);

            bannerHome.setOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    bannerIndexAdapter.setSelectIndex(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            bannerIndexAdapter = new ClassicBannerIndexAdapter(bannerDataList, bannerType);
            recyclerViewBannerIndex.setAdapter(bannerIndexAdapter);

            bannerIndexAdapter.setOnBannerIndexSelectListener(position -> {
                bannerHome.stop();
                bannerHome.setCurrentItem(position + 1);
                bannerHome.start();
                bannerIndexAdapter.setSelectIndex(position);
            });

            viewModel.mldOnPause.observe(lifecycleOwner, integer -> {
                if (integer != null && index == integer) {
                    bannerHome.onPauseEvent();
                    bannerHome.stop();
                    bannerHome.setAutoLoop(false);
                    bannerHome.setCurrentItem(bannerHome.getCurrentItem(), false);
                }
            });

            viewModel.mldOnResume.observe(lifecycleOwner, integer -> {
                if (integer != null && index == integer) {
                    if (!clBannerChild.hasFocus())
                        bannerHome.startAutoLoop(true);
                        bannerHome.replay();
                }
            });
        }
    }

    class ClassicModuleViewHolder extends RecyclerView.ViewHolder {
        private final HorizontalRecyclerView recyclerView;
        private final TextView txtTitle;
        private final ImageView imgTitleIcon;
        private final List<ProductListMo.Record> dataList;

        public void bind(ProductListType productListType, int position) {
            initObserve(productListType, productListType.appCardListLayout, position);
            txtTitle.setText(productListType.listTitle);
            if (productListType.titleIcon != 0) {
                imgTitleIcon.setImageResource(productListType.titleIcon);
            }
        }

        public ClassicModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.recycler_view_type_list);
            txtTitle = itemView.findViewById(R.id.txt_title_type_list);
            imgTitleIcon = itemView.findViewById(R.id.iv_module_item_title_icon);

            txtTitle.setTypeface(FontUtils.typefaceMedium);
            recyclerView.setLayoutManager(new CenterGridLayoutManager(itemView.getContext(), 6));
//            recyclerView.addItemDecoration(new HorizontalItemDecoration(DisplayUtil.dip2px(itemView.getContext(), 3), 0));
            recyclerView.setNestedScrollingEnabled(true);
            recyclerView.setOnHandleKeyEventListener(event -> {
                int keyCode = event.getKeyCode();
                if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                    View focusedView = recyclerView.getFocusedChild();
                    if (focusedView == null) return false;
                    View nextFocusView;
                    try {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                            nextFocusView = FocusFinder.getInstance().findNextFocus(recyclerView, focusedView, View.FOCUS_LEFT);
                        } else {
                            nextFocusView = FocusFinder.getInstance().findNextFocus(recyclerView, focusedView, View.FOCUS_RIGHT);
                        }
                    } catch (Exception e) {
                        nextFocusView = null;
                    }
                    if (nextFocusView == null) {
                        focusedView.requestFocus();
                        return true;
                    }
                }
                return false;
            });
            dataList = new ArrayList<>();
        }

        private void initObserve(ProductListType productListType, final AppCardListLayout appCardListLayout, final int position) {
            dataList.clear();
            List<ProductListMo.Record> productRecodeListMo = viewModel.getProductRecodeListFromCache(productListType.careKey);
            if (productRecodeListMo != null) {
                dataList.addAll(productRecodeListMo);
            }

            int useItemHeight = 0;
            if ("v".equals(appCardListLayout.config.previewDisplay)) {
                useItemHeight = DisplayUtil.dip2px(recyclerView.getContext(), 210);
            } else {
                useItemHeight = DisplayUtil.dip2px(recyclerView.getContext(), 126);
            }

            ClassicListAdapter adapter = new ClassicListAdapter(dataList);
            adapter.setOnCreateViewHolder(parent -> {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_type_classic_layout, parent, false);
                ClassicItemViewHolder classicItemViewHolder = new ClassicItemViewHolder(view);
                classicItemViewHolder.setTypeList(typeList);
                classicItemViewHolder.setTabIndex(index);
                if ("v".equals(appCardListLayout.config.previewDisplay)) {
                    classicItemViewHolder.setImageViewHeight(DisplayUtil.dip2px(parent.getContext(), 210));
                    /*classicItemViewHolder.setTitleSummaryMargin(DisplayUtil.dip2px(parent.getContext(), 220),
                            DisplayUtil.dip2px(parent.getContext(), 243));*/
                }
                if (!appCardListLayout.config.showAppName) {
                    classicItemViewHolder.txtTitle.setVisibility(View.GONE);
                }
                classicItemViewHolder.setOnItemFocusChange((v, hasFocus) -> {
                    if (hasFocus) {
                        if (onItemFocusedListener != null) {
                            onItemFocusedListener.onFocused(v, position);
                        }
                    }
                });
                return classicItemViewHolder;
            });

            recyclerView.setAdapter(adapter);
        }
    }

    public OnItemFocusedListener onItemFocusedListener;

    public void setOnItemFocusedListener(OnItemFocusedListener onItemFocusedListener) {
        this.onItemFocusedListener = onItemFocusedListener;
    }

    public interface OnItemFocusedListener {
        void onFocused(View v, int position);
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {

        public void bind() {
        }

        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
