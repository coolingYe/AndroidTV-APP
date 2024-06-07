package com.zwn.user.adapter;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.GlideApp;
import com.zwn.user.R;
import com.zwn.user.data.model.UserPageCommonItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UserCommonAdapter extends RecyclerView.Adapter<UserCommonAdapter.UserProductViewHolder> {
    private List<UserPageCommonItem> mItemList = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;
    private OnRemoveItemListener mOnRemoveItemListener;

    private boolean mDelMode = false;
    private boolean mDelAllMode = false;
    private Consumer<Integer> mOnItemFocusedListener;
    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    private Runnable mOnItemKeyEventUp;
    private Runnable mOnItemKeyEventDown;

    @NonNull
    @Override
    public UserCommonAdapter.UserProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_product, parent, false);
        return new UserCommonAdapter.UserProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserCommonAdapter.UserProductViewHolder holder, int position) {
        holder.bind(mItemList.get(position), position);
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public void addItem(UserPageCommonItem item) {
        mItemList.add(item);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateItemList(List<UserPageCommonItem> itemList) {
        this.mItemList = itemList;
        notifyDataSetChanged();

    }

    public void setOnItemFocusedListener(Consumer<Integer> mOnItemFocusedListener) {
        this.mOnItemFocusedListener = mOnItemFocusedListener;
    }

    public void delItem(int position) {
        mItemList.remove(position);
    }

    public void delItemBySkuId(String skuId) {
        int index = -1;
        for (int i = 0; i < mItemList.size(); i++) {
            if (skuId.equals(mItemList.get(i).skuId)) {
                mItemList.remove(i);
                index = i;
                break;
            }
        }
        if (index >= 0) {
            notifyItemRemoved(index);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDelMode(boolean delMode) {
        mDelMode = delMode;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDelAllMode(boolean delAllMode) {
        mDelAllMode = delAllMode;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearData() {
        mItemList.clear();
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, UserPageCommonItem commonItem);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnRemoveItemListener {
        void onRemove(View view, int position, UserPageCommonItem commonItem);
    }

    public void setOnRemoveItemListener(OnRemoveItemListener onRemoveItemListener) {
        mOnRemoveItemListener = onRemoveItemListener;
    }

    public void setOnItemKeyEventUpListener(Runnable mOnItemKeyEventUp) {
        this.mOnItemKeyEventUp = mOnItemKeyEventUp;
    }

    public void setOnItemKeyEventDownListener(Runnable mOnItemKeyEventDown) {
        this.mOnItemKeyEventDown = mOnItemKeyEventDown;
    }

    public Object getItem(int position) {
        return mItemList.get(position);
    }

    public class UserProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivItemUserProductImg;
        private final TextView tvItemUserProductTitle;
        private final ImageView ivItemUserProductDel;
        private final ConstraintLayout cardItemUserProduct;
        private final ImageView ivItemUserProductDelSwitch;

        public void bind(UserPageCommonItem commonItem, int position) {
            GlideApp.with(ivItemUserProductImg.getContext())
                    .load(commonItem.url)
                    .into(ivItemUserProductImg);
            ivItemUserProductDelSwitch.setVisibility(mDelAllMode ? View.VISIBLE : View.INVISIBLE);
            ivItemUserProductDel.setVisibility(mDelMode ? View.VISIBLE : View.GONE);

            if (mDelAllMode && mDelMode) {
                ivItemUserProductDel.setVisibility(View.GONE);
            }

            tvItemUserProductTitle.setText(commonItem.title);
            if (cardItemUserProduct.getNextFocusLeftId() == -1) {
                cardItemUserProduct.setNextFocusLeftId(R.id.recycler_view_user_center_category);
            }

            cardItemUserProduct.setOnClickListener(v -> {
                if (ivItemUserProductDel.getVisibility() == View.VISIBLE || mDelMode) {
                    if (mOnRemoveItemListener != null) {
                        ivItemUserProductDel.setVisibility(View.VISIBLE);
                        ivItemUserProductDel.setImageResource(R.mipmap.icon_delete_selected);
                        mOnRemoveItemListener.onRemove(v, position, commonItem);
                    }
                } else if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position, commonItem);
                }
            });

            cardItemUserProduct.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    if (mOnItemFocusedListener != null) {
                        mOnItemFocusedListener.accept(position);
                    }
                }
                if (hasFocus) {
                    tvItemUserProductTitle.setTextColor(0xFFFEA04F);
                    CommonUtils.scaleView(v, 1.1f);
                } else {
                    tvItemUserProductTitle.setTextColor(0xFFFFFFFF);
                    v.clearAnimation();
                    CommonUtils.scaleView(v, 1f);
                }
            });

            cardItemUserProduct.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (position <= 7) {
                        sHandler.post(mOnItemKeyEventUp);
                    }
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (position >= getItemCount() - 8) {
                        sHandler.post(mOnItemKeyEventDown);
                    }
                }
                return false;
            });
        }

        public UserProductViewHolder(View view) {
            super(view);
            ivItemUserProductDelSwitch = view.findViewById(R.id.iv_item_user_product_del_switch);
            ivItemUserProductImg = view.findViewById(R.id.iv_item_user_product_img);
            tvItemUserProductTitle = view.findViewById(R.id.tv_item_user_product_title);
            ivItemUserProductDel = view.findViewById(R.id.iv_item_user_product_del);
            cardItemUserProduct = view.findViewById(R.id.card_item_user_product);
        }
    }
}