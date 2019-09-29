/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * LiveGuanghan
 */
package com.snailstudio2010.earthframework.demo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.snailstudio2010.earthframework.MarkerLayout;
import com.snailstudio2010.libutils.ArrayUtils;
import com.snailstudio2010.libutils.DisplayUtils;

import java.util.List;
import java.util.Set;

/**
 * Created by xuqiqiang on 2019/09/27.
 */
public class MarkerAdapter extends MarkerLayout.Adapter<ArticlePoint, MarkerAdapter.ViewHolder> {

    private Context context;
    private List<ArticlePoint> mList;

    public MarkerAdapter(Context context, List<ArticlePoint> list) {
        this.context = context;
        this.mList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder() {
        return new MarkerAdapter.ViewHolder(context, R.layout.marker_item);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, ArticlePoint point, Set<ArticlePoint> set) {
        if (point != null) {
            holder.tvInfo.setText(point.info);
        }

        if (!ArrayUtils.isEmpty(set)) {
            holder.ivMore.setVisibility(View.VISIBLE);
        } else {
            holder.ivMore.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public ArticlePoint getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    public void setData(List<ArticlePoint> list) {
        mList = list;
        notifyDataSetChanged();
    }

    class ViewHolder extends MarkerLayout.ViewHolder {

        TextView tvInfo;
        ImageView ivMore;

        ViewHolder(Context context, int layoutId) {
            super(context, layoutId);
            tvInfo = itemView.findViewById(R.id.tv_info);
            ivMore = itemView.findViewById(R.id.iv_more);
        }

        @Override
        public int getWidth() {
            return (int) DisplayUtils.dip2px(context, 138);
        }

        @Override
        public int getHeight() {
            return (int) DisplayUtils.dip2px(context, 57);
        }

        @Override
        public float getOffsetX(int direct) {
            return 4.6f;
        }

        @Override
        public float getOffsetY(int direct) {
            return 39f;
        }
    }
}