package com.zee.launcher.home.ui.product.adapter;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;
import com.zee.launcher.home.data.protocol.response.RankingResp;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RankingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<RankingResp.RankingTopN> dataList;
    private Consumer<Integer> onItemClickListener;

    public RankingListAdapter(List<RankingResp.RankingTopN> dataList) {
        this.dataList = dataList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<RankingResp.RankingTopN> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListens(Consumer<Integer> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking_view, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CategoryViewHolder) {
            ((CategoryViewHolder) holder).bind(dataList.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public long getItemId(int position) {
        return dataList.get(position).hashCode();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTopN;
        private final TextView tvName;
        private final TextView tvResult;

        @SuppressLint("SetTextI18n")
        public void bind(RankingResp.RankingTopN rankingTopN, int position) {
            switch (rankingTopN.getRankingNum()) {
                case 1:
                    itemView.setBackgroundResource(R.mipmap.ic_ranking_item_top1_bg);
                    itemView.findViewById(R.id.tv_top_number).setVisibility(View.GONE);
                    break;
                case 2:
                    itemView.setBackgroundResource(R.mipmap.ic_ranking_item_top2_bg);
                    itemView.findViewById(R.id.tv_top_number).setVisibility(View.GONE);
                    break;
                case 3:
                    itemView.setBackgroundResource(R.mipmap.ic_ranking_item_top3_bg);
                    itemView.findViewById(R.id.tv_top_number).setVisibility(View.GONE);
                    break;
                default:
                    tvTopN.setVisibility(View.VISIBLE);
                    itemView.setBackgroundResource(R.mipmap.ic_ranking_item_bg);
                    tvTopN.setText(String.valueOf(rankingTopN.getRankingNum()));
            }
            if (rankingTopN.getUserName() != null ) {
                if (rankingTopN.getUserName().length() > 11) {
                    tvName.setText(rankingTopN.getUserName().substring(0, 11) + "...");
                } else tvName.setText(rankingTopN.getUserName());
            }
            tvResult.setText(String.valueOf(rankingTopN.getPlayScore()));
        }

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTopN = itemView.findViewById(R.id.tv_top_number);
            tvName = itemView.findViewById(R.id.tv_company_name);
            tvResult = itemView.findViewById(R.id.tv_result_value);
        }
    }
}
