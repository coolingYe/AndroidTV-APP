package com.zee.launcher.home.adapter;

import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;
import com.zee.launcher.home.data.model.MainItemMo;

import java.util.List;

public class MainItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_SELECTED = 1;
    private int selectedPosition = 0;
    private final List<MainItemMo> mainItemList;
    private OnItemClickListener onItemClickListener;
    private RecyclerView recyclerView;

    public MainItemAdapter(List<MainItemMo> mainItemList) {
        this.mainItemList = mainItemList;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == TYPE_SELECTED){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_tab_selected, parent, false);
            return new ItemViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_tab, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ItemViewHolder){
            ItemViewHolder itemViewHolder = (ItemViewHolder)holder;
            itemViewHolder.bind(mainItemList.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return mainItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(selectedPosition == position){
            return TYPE_SELECTED;
        }
        return TYPE_NORMAL;
    }

    public int getSelectedPosition(){
        return selectedPosition;
    }

    public void setFocusedPosition(int focusedPosition){
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if(layoutManager instanceof LinearLayoutManager && selectedPosition >=0 && selectedPosition <=2){
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager)layoutManager;
            View view = linearLayoutManager.getChildAt(selectedPosition);
            if(view != null){
                view.findViewById(R.id.iv_main_item_anim).setVisibility(View.INVISIBLE);
            }
        }

        int lastItemPosition = selectedPosition;
        selectedPosition = focusedPosition;
        if(lastItemPosition == selectedPosition){
            notifyItemChanged(selectedPosition);
        }else{
            if(Math.abs(lastItemPosition - selectedPosition) == 1){
                if(lastItemPosition < selectedPosition){
                    notifyItemRangeChanged(lastItemPosition, 2);
                }else{
                    notifyItemRangeChanged(selectedPosition, 2);
                }
            }else{
                notifyItemChanged(lastItemPosition);
                notifyItemChanged(selectedPosition);
            }
        }
    }

    public void setClickedPosition(int position){
        if(onItemClickListener != null){
            onItemClickListener.onItemClick(position);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{

        public ConstraintLayout rootLayout;
        private final ImageView imageViewGate;
        private final ImageView imageViewAnim;
        private final ImageView imageViewTitle;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            rootLayout = itemView.findViewById(R.id.cl_main_item_root);
            imageViewGate = itemView.findViewById(R.id.iv_main_item_gate);
            imageViewAnim = itemView.findViewById(R.id.iv_main_item_anim);
            imageViewTitle = itemView.findViewById(R.id.iv_main_item_title);

            imageViewGate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(view.getTag() != null){
                        int lastItemPosition = selectedPosition;
                        selectedPosition = ((Integer)view.getTag());
                        notifyItemChanged(lastItemPosition);
                        notifyItemChanged(selectedPosition);

                        if(onItemClickListener != null){
                            onItemClickListener.onItemClick(selectedPosition);
                        }
                    }
                }
            });
        }

        public void bind(MainItemMo mainItemMo, int position){
            imageViewGate.setTag(position);
            imageViewAnim.setVisibility(View.VISIBLE);
            if(position == selectedPosition){
                imageViewGate.setImageResource(mainItemMo.selectedImgGateResId);
                imageViewTitle.setImageResource(mainItemMo.selectedImgTitleResId);
            }else{
                imageViewGate.setImageResource(mainItemMo.imgGateResId);
                imageViewTitle.setImageResource(mainItemMo.imgTitleResId);
            }
            AnimationDrawable animationDrawable = (AnimationDrawable) imageViewAnim.getBackground();
            animationDrawable.stop();
            animationDrawable.start();
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
}
