package com.zee.device.home.ui.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.zee.device.home.databinding.ItemListQuickEntryBinding;
import com.zee.device.home.ui.home.model.QuickEntryItem;

import java.util.List;

public class QuickEntryAdapter extends RecyclerView.Adapter<QuickEntryAdapter.QuickEntryView> implements View.OnClickListener{

    private final List<QuickEntryItem> dataList;
    private QuickEntryItemClickListener quickEntryItemClickListener;

    public QuickEntryAdapter(List<QuickEntryItem> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public QuickEntryView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListQuickEntryBinding binding = ItemListQuickEntryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        return new QuickEntryView(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull QuickEntryView holder, int position) {
        holder.bind(dataList.get(position));
        holder.setItemClickListener(this);
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public void onClick(View v) {
        if(v.getTag() != null && quickEntryItemClickListener != null){
            quickEntryItemClickListener.onItemClick(v, (QuickEntryItem)v.getTag());
        }
    }

    public void setQuickEntryItemClickListener(QuickEntryItemClickListener quickEntryItemClickListener) {
        this.quickEntryItemClickListener = quickEntryItemClickListener;
    }

    static class QuickEntryView extends RecyclerView.ViewHolder{
        private final ItemListQuickEntryBinding binding;

        public QuickEntryView(@NonNull ItemListQuickEntryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(QuickEntryItem quickEntryItem){
            binding.tvQuickEntryTitle.setText(quickEntryItem.title);
            // 使用Glide加载本地资源图片
            Glide.with(binding.getRoot())
                    .load(quickEntryItem.iconResId) // 这里传入本地资源图片的资源ID
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存，如果需要缓存可以删除此行
                            .skipMemoryCache(true)) // 禁用内存缓存，如果需要缓存可以删除此行
                    .into(binding.img);
           // binding.img.setImageResource(quickEntryItem.iconResId);
            binding.getRoot().setTag(quickEntryItem);
        }

        public void setItemClickListener(View.OnClickListener onClickListener){
            binding.getRoot().setOnClickListener(onClickListener);
        }
    }

    public interface QuickEntryItemClickListener{
        void onItemClick(View view, QuickEntryItem quickEntryItem);
    }
}
