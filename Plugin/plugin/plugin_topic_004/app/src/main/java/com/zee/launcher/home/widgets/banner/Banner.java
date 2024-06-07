package com.zee.launcher.home.widgets.banner;

import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.ui.home.adapter.ClassicBannerAdapter;
import com.zee.launcher.home.widgets.MyVideoView;
import com.zee.launcher.home.widgets.banner.transformer.ScaleInTransformer;
import com.zeewain.base.utils.GlideApp;
import com.zeewain.base.utils.NetworkUtil;
import com.zeewain.base.utils.ToastUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class Banner<T, BA extends BannerAdapter<T, ? extends RecyclerView.ViewHolder>> extends CardView {
    public static final int INVALID_VALUE = -1;
    private ViewPager2 mViewPager2;
    private BA mAdapter;
    private AutoLoopTask mLoopTask;
    private BannerOnPageChangeCallback mPageChangeCallback;
    private OnPageChangeListener mOnPageChangeListener;
    private CompositePageTransformer mCompositePageTransformer;

    private boolean mIsInfiniteLoop = BannerConfig.IS_INFINITE_LOOP;
    private boolean mIsAutoLoop = BannerConfig.IS_AUTO_LOOP;
    private long mLoopTime = BannerConfig.LOOP_TIME;
    private int mScrollTime = BannerConfig.SCROLL_TIME;
    private int mStartPosition = 1;

    public Banner(@NonNull Context context) {
        this(context, null);
    }

    public Banner(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Banner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mLoopTask = new AutoLoopTask(this);
        mPageChangeCallback = new BannerOnPageChangeCallback();
        mCompositePageTransformer = new CompositePageTransformer();

        mViewPager2 = new ViewPager2(context);
        mViewPager2.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mViewPager2.setOffscreenPageLimit(2);
        mViewPager2.registerOnPageChangeCallback(mPageChangeCallback);
        mViewPager2.setPageTransformer(mCompositePageTransformer);

        /*setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        setFocusable(true);*/
        addView(mViewPager2);
    }

    /*@Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if(direction == View.FOCUS_DOWN || direction == View.FOCUS_UP){
            stop();
            setAutoLoop(true);
        }
        return super.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if(!gainFocus){
            startAutoLoop(true);
            this.clearAnimation();
            CommonUtils.scaleView(this, 1f);
        }else{
            CommonUtils.scaleView(this, 1.05f);

        }
    }*/

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int itemCount = getItemCount();
        if (event.getAction() == KeyEvent.ACTION_DOWN && itemCount >= 1) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (itemCount == 1) return true;
                int next = (getCurrentItem() + 1);
                if (next > getRealCount()) {
                    next = next - getRealCount();
                    setCurrentItem(next, false);
                } else {
                    setCurrentItem(next, true);
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (itemCount == 1) return true;
                int next = (getCurrentItem() - 1);
                if (next == 0) {
                    next = getRealCount();
                    setCurrentItem(next, false);
                } else {
                    setCurrentItem(next, true);
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                RecyclerView recyclerView = (RecyclerView) mViewPager2.getChildAt(0);
                if (recyclerView.getLayoutManager() != null) {
                    View view = recyclerView.getLayoutManager().findViewByPosition(getCurrentItem());
                    if (view != null) {
                        mAdapter.onItemEnter(view, getCurrentItem());
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!mViewPager2.isUserInputEnabled()) {
            return super.dispatchTouchEvent(ev);
        }

        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_OUTSIDE) {
            start();
        } else if (action == MotionEvent.ACTION_DOWN) {
            stop();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
        if (mCurrentHolder != null) {
            mCurrentHolder.videoView.suspend();
        }
    }

    public int getCurrentItem() {
        return mViewPager2.getCurrentItem();
    }

    public int getItemCount() {
        if (mAdapter != null) {
            return mAdapter.getItemCount();
        }
        return 0;
    }

    public void setCurrentItem(int position) {
        setCurrentItem(position, true);
    }

    public void setCurrentItem(int position, boolean smoothScroll) {
        mViewPager2.setCurrentItem(position, smoothScroll);
    }

    public int getRealCount() {
        if (mAdapter != null) {
            return mAdapter.getRealCount();
        }
        return 0;
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.mOnPageChangeListener = onPageChangeListener;
    }

    public void setOnBannerListener(OnBannerClickListener<T> listener) {
        if (getAdapter() != null) {
            getAdapter().setOnBannerClickListener(listener);
        }
    }

    public BannerAdapter getAdapter() {
        return mAdapter;
    }

    public ViewPager2 getViewPager2() {
        return mViewPager2;
    }

    public void setAdapter(BA adapter) {
        if (adapter == null) {
            throw new NullPointerException("adapter is null!");
        }
        this.mAdapter = adapter;
        mViewPager2.setAdapter(adapter);
        setCurrentItem(mStartPosition, false);
    }

    public void setDataList(List<T> dataList) {
        if (getAdapter() != null) {
            getAdapter().setDataList(dataList);
            setCurrentItem(mStartPosition, false);
            start();
        }
    }

    public Banner addPageTransformer(@Nullable ViewPager2.PageTransformer transformer) {
        mCompositePageTransformer.addTransformer(transformer);
        return this;
    }

    public Banner setBannerGalleryEffect(int leftItemWidth, int rightItemWidth, int pageMargin, float scale) {
        if (pageMargin > 0) {
            addPageTransformer(new MarginPageTransformer(pageMargin));
        }
        if (scale < 1 && scale > 0) {
            addPageTransformer(new ScaleInTransformer(scale));
        }
        setRecyclerViewPadding(leftItemWidth > 0 ? (leftItemWidth + pageMargin) : 0,
                rightItemWidth > 0 ? (rightItemWidth + pageMargin) : 0);
        return this;
    }

    private void setRecyclerViewPadding(int leftItemPadding, int rightItemPadding) {
        RecyclerView recyclerView = (RecyclerView) mViewPager2.getChildAt(0);
        if (mViewPager2.getOrientation() == ViewPager2.ORIENTATION_VERTICAL) {
            recyclerView.setPadding(mViewPager2.getPaddingLeft(), leftItemPadding, mViewPager2.getPaddingRight(), rightItemPadding);
        } else {
            recyclerView.setPadding(leftItemPadding, mViewPager2.getPaddingTop(), rightItemPadding, mViewPager2.getPaddingBottom());
        }
        recyclerView.setClipToPadding(false);
    }

    public void start() {
        if (mIsAutoLoop) {
            stop();
            postDelayed(mLoopTask, mLoopTime);
        }
    }

    public void startFast() {
        if (mIsAutoLoop) {
            stop();
            post(mLoopTask);
        }
    }

    public void stop() {
        if (mIsAutoLoop) {
            removeCallbacks(mLoopTask);
        }
    }

    public void setAutoLoop(boolean autoLoop) {
        this.mIsAutoLoop = autoLoop;
    }

    public void startAutoLoop(boolean autoLoop) {
        this.mIsAutoLoop = autoLoop;
        start();
    }

    private ClassicBannerAdapter.BannerVideoViewHolder mCurrentHolder;

    public void replay() {
        if (mCurrentHolder != null) {
            if (hasVideoItem()) {
                RecyclerView recyclerView = (RecyclerView) mViewPager2.getChildAt(0);
                if (recyclerView.getLayoutManager() != null) {
                    View view = recyclerView.getLayoutManager().findViewByPosition(getCurrentItem());
                    if (view != null) {
                        String uri = getVideoItemData(getCurrentItem()).getResourceUrl();
                        if (!TextUtils.isEmpty(uri))
                            autoVideoPlay(uri, view);
                    }
                }
            }
        }
    }

    class BannerOnPageChangeCallback extends ViewPager2.OnPageChangeCallback {
        private int mTempPosition = INVALID_VALUE;
        private boolean isScrolled;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int realPosition = mAdapter.getRealPosition(position);
            if (mOnPageChangeListener != null && realPosition == getCurrentItem() - 1) {
                mOnPageChangeListener.onPageScrolled(realPosition, positionOffset, positionOffsetPixels);
            }

            if (mCurrentHolder != null) {
                mCurrentHolder.videoView.pause();
                mCurrentHolder.imgClassicBanner.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onPageSelected(int position) {
            mTempPosition = position;
            int realPosition = mAdapter.getRealPosition(position);
            if (mOnPageChangeListener != null) {
                mOnPageChangeListener.onPageSelected(realPosition);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            //手势滑动中,代码执行滑动中
            if (state == ViewPager2.SCROLL_STATE_DRAGGING || state == ViewPager2.SCROLL_STATE_SETTLING) {
                isScrolled = true;
            } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                //滑动闲置或滑动结束
                isScrolled = false;
                if (mTempPosition != INVALID_VALUE && mIsInfiniteLoop) {
                    if (mTempPosition == 0) {
                        setCurrentItem(getRealCount(), false);
                    } else if (mTempPosition == getItemCount() - 1) {
                        setCurrentItem(1, false);
                    }
                }
            }

            if (mOnPageChangeListener != null) {
                mOnPageChangeListener.onPageScrollStateChanged(state);
            }

            if (!hasVideoItem()) return;
            if (state == RecyclerView.SCROLL_STATE_IDLE) {
                RecyclerView recyclerView = (RecyclerView) mViewPager2.getChildAt(0);
                if (recyclerView.getLayoutManager() != null) {
                    View view = recyclerView.getLayoutManager().findViewByPosition(getCurrentItem());
                    if (view != null) {
                        String uri = getVideoItemData(getCurrentItem()).getResourceUrl();
                        if (!TextUtils.isEmpty(uri))
                            autoVideoPlay(uri, view);
                    }
                }
            }
        }
    }

    private ProductListMo.Record getVideoItemData(int position) {
        return ((ProductListMo.Record) mAdapter.dataList.get(mAdapter.getRealPosition(position)));
    }

    public boolean hasVideoItem() {
        return getVideoItemData(getCurrentItem()).getKind().equals("video");
    }

    public boolean hasVideoPlaying() {
        if (mCurrentHolder != null) {
            return mCurrentHolder.videoView.isPlaying();
        }
        return false;
    }

    private void autoVideoPlay(String videoUri, View view) {
        stop();
        mCurrentHolder = new ClassicBannerAdapter.BannerVideoViewHolder(view);

        ((ViewGroup) mCurrentHolder.itemView).setLayoutTransition(new LayoutTransition());
        //Set the parent layout do not follow the animate of the subviews.
        ((ViewGroup) mCurrentHolder.itemView).getLayoutTransition().setAnimateParentHierarchy(false);

        mCurrentHolder.imgClassicBanner.setVisibility(VISIBLE);

        //Get the first video frame as cover.
        mCurrentHolder.imgClassicBanner.setScaleType(ImageView.ScaleType.FIT_XY);
        GlideApp.with(mCurrentHolder.imgClassicBanner.getContext())
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .frame(0))
                .load(videoUri)
                .into(mCurrentHolder.imgClassicBanner);

        checkVideoCache(mCurrentHolder.videoView, videoUri);

        mCurrentHolder.videoView.setOnErrorListener((mp, what, extra) -> {
            ToastUtils.showShort("视频播放出错");
            return true;
        });

        mCurrentHolder.videoView.setOnPreparedListener(mp -> {
            mCurrentHolder.videoView.start();
            mp.setOnInfoListener((mp1, what, extra) -> {
                if (what == MEDIA_INFO_VIDEO_RENDERING_START) {
                    mCurrentHolder.imgClassicBanner.setVisibility(GONE);
                    return true;
                }
                return false;
            });
        });

        mCurrentHolder.videoView.setOnCompletionListener(mp -> {
            startFast();
            mCurrentHolder.imgClassicBanner.setVisibility(VISIBLE);
        });
    }

    private void checkVideoCache(View view, String videoUri) {
        Uri cacheVideoPath = Uri.fromFile(new File(GlideApp.getPhotoCacheDir(view.getContext()), videoUri));
        if (!NetworkUtil.isNetworkAvailable(view.getContext()) && cacheVideoPath.toString().isEmpty()) {
            return;
        }
        GlideApp.with(view.getContext())
                .asFile()
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA))
                .load(videoUri)
                .into(new CustomTarget<File>() {
                    @Override
                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                        ((MyVideoView) view).setVideoURI(Uri.fromFile(resource));
                        ((MyVideoView) view).setVisibility(VISIBLE);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                });
    }

    public void onPauseEvent() {
        if (mCurrentHolder != null) {
            mCurrentHolder.imgClassicBanner.setVisibility(VISIBLE);
        }
    }

    class AutoLoopTask implements Runnable {
        private final WeakReference<Banner> reference;

        AutoLoopTask(Banner banner) {
            this.reference = new WeakReference<>(banner);
        }

        @Override
        public void run() {
            Banner banner = reference.get();
            if (banner != null && banner.mIsAutoLoop) {
                int count = banner.getItemCount();
                if (count == 0) {
                    return;
                }
                int next = (banner.getCurrentItem() + 1) % count;
                banner.setCurrentItem(next);
                banner.postDelayed(banner.mLoopTask, banner.mLoopTime);
            }
        }
    }
}
