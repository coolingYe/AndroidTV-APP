package com.zee.launcher.home.ui.home.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;
import com.zee.launcher.home.ui.home.model.ModuleMenuMo;


import java.util.List;

public class ModuleListMenuAdapter extends RecyclerView.Adapter<ModuleListMenuAdapter.MenuItemViewHolder> {
    private final List<ModuleMenuMo> dataList;
    private final OnMenuSelectListener onMenuSelectListener;

    public ModuleListMenuAdapter(List<ModuleMenuMo> dataList, OnMenuSelectListener onMenuSelectListener) {
        this.dataList = dataList;
        this.onMenuSelectListener = onMenuSelectListener;
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_module_list_menu_layout, parent, false);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        holder.bind(dataList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class MenuItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView textView;

        public void bind(ModuleMenuMo moduleMenuMo, int position){
            textView.setText(moduleMenuMo.title);
            textView.setTag(position);
        }

        public MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.txt_menu_title);
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(onMenuSelectListener != null && v.getTag() != null){
                Integer position = (Integer) v.getTag();
                onMenuSelectListener.onMenuSelect(position);
            }
        }
    }
}
