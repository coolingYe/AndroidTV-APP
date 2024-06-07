package com.zee.launcher.home.ui.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


import com.zee.launcher.home.R;
import com.zee.launcher.home.config.ProdConstants;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.ui.home.holder.ClassicItemViewHolder;
import com.zee.paged.adapter.HorizontalPagedListAdapter;
import com.zee.paged.adapter.IPagedLoadMoreListener;
import com.zeewain.base.widgets.SmallLoadingView;

import java.util.List;


public class ClassicPagedListAdapter extends HorizontalPagedListAdapter<ProductListMo.Record> {
    private final IPagedLoadMoreListener<ProductListMo.Record> pagedLoadMoreListener;
    private OnCreateViewHolder onCreateViewHolder;
    private final int itemHeight;

    public ClassicPagedListAdapter(List<ProductListMo.Record> dataList, IPagedLoadMoreListener<ProductListMo.Record> pagedLoadMoreListener, int itemHeight) {
        super(dataList, ProdConstants.PRD_PAGE_SIZE);
        this.pagedLoadMoreListener = pagedLoadMoreListener;
        this.itemHeight = itemHeight;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_NORMAL) {
            if(onCreateViewHolder != null){
                return onCreateViewHolder.createNormalViewHolder(parent);
            }else{
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_type_classic_layout, parent, false);
                return new ClassicItemViewHolder(view);
            }
        }else if(viewType == TYPE_LOADING){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_type_loading_failed_layout, parent, false);
            return new LoadingItemViewHolder(view);
        }else if(viewType == TYPE_LOAD_FAILED){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_type_loading_failed_layout, parent, false);
            return new LoadFailedItemViewHolder(view);
        }else if(viewType == TYPE_DONE){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_type_idle_footer_layout, parent, false);
            return new DoneItemViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_type_idle_footer_layout, parent, false);
            return new IdleItemViewHolder(view);
        }
    }

    @Override
    public void onBindView(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ClassicItemViewHolder){
            ((ClassicItemViewHolder)holder).bind(dataList.get(position));
        }else if(holder instanceof LoadingItemViewHolder){
            ((LoadingItemViewHolder)holder).bind(itemHeight);
        }else if(holder instanceof LoadFailedItemViewHolder){
            ((LoadFailedItemViewHolder)holder).bind(itemHeight);
        }else if(holder instanceof DoneItemViewHolder){
            ((DoneItemViewHolder)holder).bind(itemHeight);
        }else {
            ((IdleItemViewHolder)holder).bind(itemHeight);
        }
    }

    public void setOnCreateViewHolder(OnCreateViewHolder onCreateViewHolder) {
        this.onCreateViewHolder = onCreateViewHolder;
    }

    @Override
    public boolean onLoadMore() {
        return pagedLoadMoreListener.onLoadMore();
    }

    @Override
    public List<ProductListMo.Record> getLoadedData() {
        return pagedLoadMoreListener.getLoadedData();
    }

    static class LoadingItemViewHolder extends RecyclerView.ViewHolder {
        private final SmallLoadingView loadingView;
        private final LinearLayout networkErrView;
        private final ConstraintLayout lightLayoutTypeRecommend;
        private final LinearLayout rootLayout;

        public void bind(int itemHeight){
            ViewGroup.LayoutParams layoutParams = rootLayout.getLayoutParams();
            layoutParams.height = itemHeight;
            showLoading();
        }

        public LoadingItemViewHolder(@NonNull View view) {
            super(view);
            loadingView = view.findViewById(R.id.loadingView_type_recommend);
            networkErrView = view.findViewById(R.id.ll_netErr_type_recommend);
            lightLayoutTypeRecommend = view.findViewById(R.id.lightLayout_type_recommend);
            rootLayout = view.findViewById(R.id.ll_type_recommend_root);
        }


        public void showLoading(){
            loadingView.setVisibility(View.VISIBLE);
            loadingView.startAnim();
            networkErrView.setVisibility(View.GONE);
        }
    }

    static class LoadFailedItemViewHolder extends RecyclerView.ViewHolder {
        private final SmallLoadingView loadingView;
        private final LinearLayout networkErrView;
        private final ConstraintLayout lightLayoutTypeRecommend;
        private final LinearLayout rootLayout;

        public void bind(int itemHeight){
            ViewGroup.LayoutParams layoutParams = rootLayout.getLayoutParams();
            layoutParams.height = itemHeight;
            showLoadedErr();
        }

        public LoadFailedItemViewHolder(@NonNull View view) {
            super(view);
            loadingView = view.findViewById(R.id.loadingView_type_recommend);
            networkErrView = view.findViewById(R.id.ll_netErr_type_recommend);
            lightLayoutTypeRecommend = view.findViewById(R.id.lightLayout_type_recommend);
            rootLayout = view.findViewById(R.id.ll_type_recommend_root);
        }

        public void showLoadedErr(){
            loadingView.setVisibility(View.GONE);
            loadingView.stopAnim();
            networkErrView.setVisibility(View.VISIBLE);
        }

    }

    static class DoneItemViewHolder extends RecyclerView.ViewHolder{
        public void bind(int itemHeight){}

        public DoneItemViewHolder(@NonNull View view) {
            super(view);
        }

    }

    static class IdleItemViewHolder extends RecyclerView.ViewHolder{

        public void bind(int itemHeight){}

        public IdleItemViewHolder(@NonNull View view) {
            super(view);
        }
    }

    public interface OnCreateViewHolder{
        ClassicItemViewHolder createNormalViewHolder(ViewGroup parent);
    }
}
