/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Earth Framework
 */
package com.snailstudio2010.earthframework.demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.snailstudio2010.earthframework.EarthView;
import com.snailstudio2010.earthframework.MarkerLayout;
import com.snailstudio2010.earthframework.MarkerPoint;
import com.snailstudio2010.earthframework.adapter.MarkerAdapter;
import com.snailstudio2010.earthframework.entity.ArticlePoint;
import com.snailstudio2010.libutils.ScreenUtils;
import com.snailstudio2010.libutils.StatusBarUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.snailstudio2010.earthframework.EarthUtils.logD;

/**
 * Created by xuqiqiang on 2019/08/12.
 */
public class EarthActivity extends AppCompatActivity implements MarkerLayout.OnMarkerTapListener {
    private static final String TAG = EarthActivity.class.getSimpleName();

    private EarthView mEarthView;
    private MarkerAdapter mMarkerAdapter;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.initialize(this);
        setContentView(R.layout.activity_earth);

        StatusBarUtils.setRootViewFitsSystemWindows(this, false);
        StatusBarUtils.setTranslucentStatus(this);
        StatusBarUtils.setStatusBarDarkTheme(this, false);

        mEarthView = findViewById(R.id.earthView);
        mEarthView.init(this::onRefreshMarkers);
        mEarthView.setOnMarkerTapListener(this);

        ImageView ivRefresh = findViewById(R.id.iv_refresh);
        ivRefresh.setOnClickListener(v -> {
            if (mMarkerAdapter != null) mMarkerAdapter.clear();
            mEarthView.resetMap(() -> mHandler.postDelayed(this::onRefreshMarkers, 1000));
        });
    }

    private void onRefreshMarkers() {

        String[] infos = {
                "福岛储存了多少污染水，竟然要排入海中",
                "迪拜世博会的中国“硬实力”和“软实力”",
                "无人机炸掉了沙特石油一半产能",
                "世界上最美城堡的奇幻记忆",
                "超级工程“中国天眼”牛在哪？",
                "12000年历史的土耳其古镇即将被吞没",

                "福岛储存了多少污染水，竟然要排入海中",
                "迪拜世博会的中国“硬实力”和“软实力”",
                "无人机炸掉了沙特石油一半产能",
                "世界上最美城堡的奇幻记忆",
                "超级工程“中国天眼”牛在哪？",
                "12000年历史的土耳其古镇即将被吞没",
        };
        String[] photos = {
                null,
                "http://earthlive-imgs.national-space.com/FjAvDfPQzBJUU4U1Tea7RUpR-Amk",
                "http://earthlive-imgs.national-space.com/09-19/WGAvzrlHcEnq6Qxd0RvVA.png",
                "http://cdn.national-space.com/09-17/pViTphZXyw9fpZsLBQtFi.png",
        };
        List<ArticlePoint> list = new ArrayList<>();
        for (String info : infos) {
            list.add(new ArticlePoint(Math.random() * 180, Math.random() * 70, info, photos[new Random().nextInt(photos.length)]));
        }

        if (mMarkerAdapter == null) {
            mMarkerAdapter = new MarkerAdapter(this, list);
            mEarthView.setAdapter(mMarkerAdapter);
        } else {
            mMarkerAdapter.setData(list);
        }
    }

    @Override
    public void onMarkerTap(MarkerPoint hashPoint, Set<MarkerPoint> set) {

        logD("onMarkerTap:" + hashPoint);
        mEarthView.flyToMarker(hashPoint, set);

//        if (hashPoint instanceof ArticlePoint)
//            mGalleryView.show(ArrayUtils.createList(((ArticlePoint) hashPoint).mArticleItem));
    }

    @Override
    public void onMapStop() {

    }
}
