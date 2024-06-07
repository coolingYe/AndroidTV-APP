package com.zee.device.home.ui.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.device.home.databinding.ItemListCourseEntryBinding;
import com.zee.device.home.databinding.ItemListQuickEntryBinding;
import com.zee.device.home.ui.home.model.CourseEntryItem;
import com.zee.device.home.ui.home.model.QuickEntryItem;

import java.util.List;

public class CourseEntryAdapter extends RecyclerView.Adapter<CourseEntryAdapter.CourseEntryView> implements View.OnClickListener{

    private final List<CourseEntryItem> dataList;
    private CourseEntryItemClickListener courseEntryItemClickListener;

    public CourseEntryAdapter(List<CourseEntryItem> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public CourseEntryView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListCourseEntryBinding binding = ItemListCourseEntryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        return new CourseEntryView(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseEntryView holder, int position) {
        holder.bind(dataList.get(position));
        holder.setItemClickListener(this);
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public void onClick(View v) {
        if(v.getTag() != null && courseEntryItemClickListener != null){
            courseEntryItemClickListener.onItemClick(v, (CourseEntryItem)v.getTag());
        }
    }

    public void setCourseEntryItemClickListener(CourseEntryItemClickListener courseEntryItemClickListener) {
        this.courseEntryItemClickListener = courseEntryItemClickListener;
    }

    static class CourseEntryView extends RecyclerView.ViewHolder{
        private final ItemListCourseEntryBinding binding;

        public CourseEntryView(@NonNull ItemListCourseEntryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CourseEntryItem courseEntryItem){
            binding.tvCourseEntryTitle.setText(courseEntryItem.title);
            binding.imgCourse.setImageResource(courseEntryItem.iconResId);
            binding.getRoot().setTag(courseEntryItem);
        }

        public void setItemClickListener(View.OnClickListener onClickListener){
            binding.getRoot().setOnClickListener(onClickListener);
        }
    }

    public interface CourseEntryItemClickListener {
        void onItemClick(View view, CourseEntryItem courseEntryItem);
    }
}
