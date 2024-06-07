package com.zee.launcher.home.ui.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.zee.launcher.home.R;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.ui.home.holder.ClassicItemViewHolder;
import java.util.List;


public class ClassicListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private OnCreateViewHolder onCreateViewHolder;
    private final List<ProductListMo.Record> dataList;

    public ClassicListAdapter(List<ProductListMo.Record> dataList) {
        this.dataList = dataList;
    }

    public void setOnCreateViewHolder(OnCreateViewHolder onCreateViewHolder) {
        this.onCreateViewHolder = onCreateViewHolder;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(onCreateViewHolder != null){
            return onCreateViewHolder.createNormalViewHolder(parent);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_type_classic_layout, parent, false);
            return new ClassicItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ClassicItemViewHolder){
            ((ClassicItemViewHolder)holder).bind(dataList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface OnCreateViewHolder{
        ClassicItemViewHolder createNormalViewHolder(ViewGroup parent);
    }
}
