package com.zee.paged.adapter;


import android.annotation.SuppressLint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.paged.HorizontalRecyclerView;
import com.zee.paged.model.FooterState;
import com.zee.paged.model.PagedListLoadState;
import com.zeewain.base.model.LoadState;

import java.util.List;

public abstract class HorizontalPagedListAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements IPagedViewHolder, IPagedLoadMoreListener<T> {
    private static final String TAG = "PagedAdapter";
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_LOADING = -1;
    public static final int TYPE_DONE = -2;
    public static final int TYPE_LOAD_FAILED = -3;
    public static final int TYPE_IDLE = -4;

    public List<T> dataList;
    public final int pageSize;

    private FooterState footerState = FooterState.Wait;
    private boolean isSlidingLeftward = false;
    private int scrollState = RecyclerView.SCROLL_STATE_IDLE;
    private RecyclerView recyclerView;
    private boolean isAllLoadedDone = false;

    public HorizontalPagedListAdapter(List<T> dataList, int pageSize){
        this.dataList = dataList;
        this.pageSize = pageSize;
        if(dataList == null){
            throw new RuntimeException("dataList = null");
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return onCreateHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        onBindView(holder,  position);
    }

    @Override
    public int getItemCount() {
        return (dataList.size() < pageSize) ? dataList.size() : dataList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == dataList.size()) {
            if(footerState == FooterState.Loading){
                return TYPE_LOADING;
            }else if(footerState == FooterState.Wait){
                if(isAllLoadedDone){
                    return TYPE_DONE;
                }else{
                    return TYPE_LOADING;
                }
            }else if(footerState == FooterState.Failed){
                return TYPE_LOAD_FAILED;
            }else if(footerState == FooterState.Done){
                return TYPE_DONE;
            }else{
                return TYPE_IDLE;
            }
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        recyclerView.addOnScrollListener(onScrollListener);
        if(recyclerView instanceof HorizontalRecyclerView){
            final HorizontalRecyclerView horizontalRecyclerView = ((HorizontalRecyclerView)recyclerView);
            horizontalRecyclerView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    Log.i("focus", "--OnKeyListener---" + event);
                    return false;
                }
            });

            horizontalRecyclerView.setOnHandleKeyEventListener(event -> {
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    int keyCode = event.getKeyCode();
                    if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                        View focusedView = horizontalRecyclerView.getFocusedChild();
                        RecyclerView.LayoutManager layoutManager = horizontalRecyclerView.getLayoutManager();
                        if(layoutManager instanceof LinearLayoutManager){
                            int focusPosition  = layoutManager.getPosition(focusedView);
                            if(footerState == FooterState.Idle || footerState == FooterState.Wait) {
                                footerState = FooterState.Wait;
                                int itemCount = layoutManager.getItemCount();
                                notifyItemChanged(itemCount-1);
                            }

                            if(focusPosition == recyclerView.getLayoutManager().getItemCount() -1){
                                focusedView.requestFocus();
                                return true;
                            }else if(isAllLoadedDone && focusPosition == recyclerView.getLayoutManager().getItemCount() -2){
                                focusedView.requestFocus();
                                return true;
                            }
                        }
                    }else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
                        View focusedView = horizontalRecyclerView.getFocusedChild();
                        RecyclerView.LayoutManager layoutManager = horizontalRecyclerView.getLayoutManager();
                        if(layoutManager instanceof LinearLayoutManager){
                            int focusPosition  = layoutManager.getPosition(focusedView);
                            if(focusPosition == 0){
                                focusedView.requestFocus();
                                return true;
                            }
                        }
                    }
                }
                return false;
            });
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.removeOnScrollListener(onScrollListener);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            scrollState = newState;
            if(isAllLoadedDone) return;
            if(newState == RecyclerView.SCROLL_STATE_IDLE && footerState == FooterState.Wait){
                Log.d(TAG, "onScrollStateChanged() IDLE footerState="+ footerState);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if(layoutManager instanceof LinearLayoutManager) {
                    int lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
                    int itemCount = layoutManager.getItemCount();
                    if(lastVisibleItemPosition == layoutManager.getItemCount() - 2){
                        footerState = FooterState.Idle;
                        notifyItemChanged(itemCount-1);
                    }
                }

                if(isSlidingLeftward) {
                    Log.d(TAG, "onScrollStateChanged() IDLE footerState="+ footerState + ", checkCanLoad() call");
                    checkCanLoad(recyclerView);
                }

            }else if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
                Log.d(TAG, "onScrollStateChanged() DRAGGING footerState="+ footerState);
                if(footerState == FooterState.Idle || footerState == FooterState.Wait) {
                    footerState = FooterState.Wait;
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    if(layoutManager instanceof LinearLayoutManager) {
                        int itemCount = layoutManager.getItemCount();
                        notifyItemChanged(itemCount-1);
                    }
                }
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            isSlidingLeftward = (dx >= 0);
            if(isAllLoadedDone) return;
            if(isSlidingLeftward && footerState == FooterState.Wait
                    && scrollState == RecyclerView.SCROLL_STATE_IDLE
                    && !recyclerView.canScrollHorizontally(1)){
                Log.d(TAG, "onScrolled() footerState="+ footerState + ", checkCanLoad() call");
                checkCanLoad(recyclerView);
            }
        }
    };

    private synchronized void checkCanLoad(RecyclerView recyclerView){
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if(layoutManager instanceof LinearLayoutManager){
            int lastVisibleItemPosition = ((LinearLayoutManager)layoutManager).findLastCompletelyVisibleItemPosition();
            int itemCount = layoutManager.getItemCount();
            if(lastVisibleItemPosition == layoutManager.getItemCount() - 1){
                boolean isLoading = onLoadMore();
                if(isLoading){
                    footerState = FooterState.Loading;
                    notifyItemChanged(itemCount-1);
                }else{
                    Log.d(TAG, "---------->>>>> checkCanLoad() isLoading false");
                    if(isAllLoadedDone){
                        Log.e(TAG, "---------->>>>> checkCanLoad() isLoading false isAllLoadedDone");
                    }
                }
            }
        }
    }

    public void setFooterState(FooterState footerState) {
        this.footerState = footerState;
    }

    public void setFooterStateNotifyLastChange(FooterState footerState) {
        Log.d(TAG, "setFooterStateNotifyLastChange() footerState==" + footerState);
        this.footerState = footerState;
        if(scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            notifyItemChanged(getItemCount() - 1);
            Log.d(TAG, "setFooterStateNotifyLastChange() footerState==" + footerState + ", notifyItemChanged done");
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void initViewObserve(final String pagedListId, LiveData<PagedListLoadState<T>> loadStateLiveData, LifecycleOwner lifecycleOwner){
        if(dataList.size() > 0 && dataList.size() < pageSize){
            isAllLoadedDone = true;
            setFooterState(FooterState.Done);
        }

        loadStateLiveData.observe(lifecycleOwner, pagedListLoadState -> {
            if(pagedListId.equals(pagedListLoadState.pagedListId)) {
                if(pagedListLoadState.pageNum == 1)
                    onFirstLoadState(pagedListLoadState.loadState);
                if(pagedListLoadState.loadState == LoadState.Loading){
                    setFooterStateNotifyLastChange(FooterState.Loading);
                }else if(pagedListLoadState.loadState == LoadState.Success) {
                    setFooterState(FooterState.Wait);
                    if(pagedListLoadState.dataList.size() < pageSize){
                        isAllLoadedDone = true;
                        setFooterState(FooterState.Done);
                    }
                    dataList.clear();
                    dataList.addAll(getLoadedData());
                    notifyDataSetChanged();
                }else{
                    setFooterStateNotifyLastChange(FooterState.Failed);
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setFooterStateNotifyLastChange(FooterState.Idle);
                        }
                    }, 500);
                }
            }
        });
    }

    public void onFirstLoadState(LoadState loadState){

    }

}
