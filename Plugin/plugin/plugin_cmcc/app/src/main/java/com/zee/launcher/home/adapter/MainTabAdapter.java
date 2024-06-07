package com.zee.launcher.home.adapter;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.data.layout.PageLayoutDTO;

import java.util.List;
import java.util.Objects;

import com.zee.launcher.home.R;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.FontUtils;

public class MainTabAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_SELECTED = 1;
    private int selectedPosition = 0;
    private int orientation = LinearLayoutManager.VERTICAL;
    private final List<PageLayoutDTO> mainCategoryList;
    private OnSelectedListener onSelectedListener;
    private RecyclerView recyclerView;

    public MainTabAdapter(List<PageLayoutDTO> mainCategoryList) {
        this.mainCategoryList = mainCategoryList;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager != null) {
            orientation = ((LinearLayoutManager) layoutManager).getOrientation();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SELECTED) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_tab_selected, parent, false);
            return new TabSelectedViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_tab, parent, false);
            return new TabViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TabViewHolder) {
            TabViewHolder tabViewHolder = (TabViewHolder) holder;
            tabViewHolder.bind(mainCategoryList.get(position), position);
            tabViewHolder.llMainTabRoot.setOnClickListener(view -> {
                if (onSelectedListener != null) {
                    onSelectedListener.onSelected(holder.getAdapterPosition());
                }
            });
        } else {
            TabSelectedViewHolder tabSelectedViewHolder = (TabSelectedViewHolder) holder;
            tabSelectedViewHolder.bind(mainCategoryList.get(position), position);
            tabSelectedViewHolder.llMainTabRoot.setOnClickListener(view -> {
                if (onSelectedListener != null) {
                    onSelectedListener.onSelected(holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mainCategoryList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (selectedPosition == position) {
            return TYPE_SELECTED;
        }
        return TYPE_NORMAL;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectedPosition(int selectedPosition) {
        //int lastSelectedPosition = this.selectedPosition;
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
        //notifyItemChanged(lastSelectedPosition);
        //notifyItemChanged(this.selectedPosition);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setFocusedPosition(int focusedPosition) {
        if (onSelectedListener != null) {
            onSelectedListener.onSelected(focusedPosition);
        }
    }

    public void setOnSelectedListener(OnSelectedListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
    }

    class TabViewHolder extends RecyclerView.ViewHolder {
        public TextView txtListTab;
        public LinearLayout llMainTabRoot;

        public void bind(PageLayoutDTO pagesDTO, int position) {
            txtListTab.setText(pagesDTO.name);
            txtListTab.setTextColor(0xFFFFFFFF);
            txtListTab.setTypeface(FontUtils.typefaceMedium);
            llMainTabRoot.setBackground(null);
            llMainTabRoot.setTag(position);
        }

        public TabViewHolder(@NonNull View view) {
            super(view);
            txtListTab = view.findViewById(R.id.txt_list_tab);
            llMainTabRoot = view.findViewById(R.id.ll_main_tab_root);
        }
    }

    class TabSelectedViewHolder extends RecyclerView.ViewHolder {
        public TextView txtListTab;
        public LinearLayout llMainTabRoot;

        public void bind(PageLayoutDTO pagesDTO, int position) {
            txtListTab.setText(pagesDTO.name);
            txtListTab.setTypeface(FontUtils.typefaceHeavy);
            txtListTab.setTextColor(0xFFFFFFFF);
            if (recyclerView.hasFocus()) {
                if (CommonUtils.isTopic003Enable()) {
                    if (Objects.equals(pagesDTO.name, "党建")) {
                        llMainTabRoot.setBackgroundResource(R.mipmap.ic_main_tab_seleted_cmcc1_tab2_bg);
                    } else
                        llMainTabRoot.setBackgroundResource(R.mipmap.ic_main_tab_seleted_cmcc1_bg);
                }
            } else {
                llMainTabRoot.setBackground(null);
            }
            llMainTabRoot.setTag(position);
        }

        public TabSelectedViewHolder(@NonNull View view) {
            super(view);
            txtListTab = view.findViewById(R.id.txt_list_tab);
            llMainTabRoot = view.findViewById(R.id.ll_main_tab_root);
            txtListTab.setTypeface(FontUtils.typefaceHeavy);
        }
    }

    public interface OnSelectedListener {
        void onSelected(int position);
    }
}
