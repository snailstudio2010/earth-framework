/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Earth Framework
 */
package com.snailstudio2010.earthframework.demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.snailstudio2010.earthframework.EarthUtils;
import com.snailstudio2010.earthframework.MarkerLayout;
import com.snailstudio2010.earthframework.MarkerPoint;
import com.snailstudio2010.earthframework.adapter.MarkerAdapter;
import com.snailstudio2010.earthframework.entity.ArticlePoint;
import com.snailstudio2010.libutils.ArrayUtils;
import com.snailstudio2010.libutils.ScreenUtils;
import com.snailstudio2010.libutils.StatusBarUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.snailstudio2010.earthframework.EarthUtils.logD;

/**
 * Created by xuqiqiang on 2019/08/12.
 */
public class MainActivity extends AppCompatActivity implements MarkerLayout.OnMarkerTapListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private SceneView mSceneView;
    private View mMask;
    private MarkerLayout mMarkerLayout;
    private MarkerAdapter mMarkerAdapter;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.initialize(this);
        setContentView(R.layout.activity_main);

        StatusBarUtils.setRootViewFitsSystemWindows(this, false);
        StatusBarUtils.setTranslucentStatus(this);
        StatusBarUtils.setStatusBarDarkTheme(this, false);

        mSceneView = findViewById(R.id.sceneView);
        mMask = findViewById(R.id.mask);
        setupMap();

        ImageView ivRefresh = findViewById(R.id.iv_refresh);
        ivRefresh.setOnClickListener(v -> {
            if (mMarkerAdapter != null) mMarkerAdapter.clear();
            Utils.resetMap(mSceneView, () -> mHandler.postDelayed(this::onRefreshMarkers, 1000));
        });
    }

    private void setupMap() {
        if (mSceneView != null) {

            ArcGISScene scene = new ArcGISScene(Basemap.Type.IMAGERY);
//            scene.setBasemap(Basemap.createImagery());

//            ArcGISScene scene = new ArcGISScene(ArcGISScene.SceneViewTilingScheme.GEOGRAPHIC);
//            GoogleLayerBuilder builder = new GoogleLayerBuilder();
//            GoogleLayerImage imageLayer = builder.CreateImageLayer();
//            scene.getOperationalLayers().add(imageLayer);
//            GoogleLayerLabel labellayer = builder.CreateLabelLayer();
//            scene.getOperationalLayers().add(labellayer);

            mSceneView.setScene(scene);
            Camera camera = new Camera(Constants.mLatitude, Constants.mLongitude, Constants.mAltitude,
                    Constants.mHeading, Constants.mPitch, Constants.mRoll);
            mSceneView.setViewpointCamera(camera);
            mSceneView.setOnTouchListener(new SceneViewOnTouchListener(mSceneView));
            scene.addDoneLoadingListener(() ->
                    mHandler.postDelayed(this::onMapReady, 1000));
        }
    }

    private void onMapReady() {
        mMask.setVisibility(View.GONE);
        mMarkerLayout = new MarkerLayout(this, mSceneView);
        mMarkerLayout.setOnMarkerTapListener(this);

        onRefreshMarkers();
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
        List<ArticlePoint> list = new ArrayList<>();
        for (String info : infos) {
            list.add(new ArticlePoint(Math.random() * 180, Math.random() * 70, info, null));
        }

        if (mMarkerAdapter == null) {
            mMarkerAdapter = new MarkerAdapter(this, list);
            mMarkerLayout.setAdapter(mMarkerAdapter);
        } else {
            mMarkerAdapter.setData(list);
        }
    }

    @Override
    public void onMarkerTap(MarkerPoint hashPoint, Set<MarkerPoint> set) {

        logD("onMarkerTap:" + hashPoint);
        Point point = mSceneView.getCurrentViewpointCamera().getLocation();

        double targetZ = Constants.mAltitudes[Constants.mAltitudes.length - 1];
        if (!ArrayUtils.isEmpty(set)) {
            targetZ = Utils.getTargetAltitude(point);
        }
        Point target = new Point(hashPoint.x, hashPoint.y, targetZ);
        EarthUtils.moveMap(mSceneView, target, Utils.calcDuration(targetZ));

//        if (hashPoint instanceof ArticlePoint)
//            mGalleryView.show(ArrayUtils.createList(((ArticlePoint) hashPoint).mArticleItem));
    }

    @Override
    public void onMapStop() {

    }

    class SceneViewOnTouchListener extends DefaultSceneViewOnTouchListener {

        public SceneViewOnTouchListener(SceneView sceneView) {
            super(sceneView);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (mMarkerLayout != null)
                mMarkerLayout.onTouch(motionEvent);
            return super.onTouch(view, motionEvent);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mMarkerLayout != null && mMarkerLayout.onSingleTap(e))
                return super.onSingleTapConfirmed(e);
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            if (mMarkerLayout != null)
                mMarkerLayout.onScaleEnd();
            super.onScaleEnd(scaleGestureDetector);
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }
    }
}
