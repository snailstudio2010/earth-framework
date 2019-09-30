package com.snailstudio2010.earthframework;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.snailstudio2010.libutils.ArrayUtils;
import com.snailstudio2010.libutils.NotNull;

import java.util.Set;


public class EarthView extends RelativeLayout {

    private Context mContext;
    private ArcGISScene mScene;
    private double mLatitude = Constants.mLatitude;
    private double mLongitude = Constants.mLongitude;
    private double mAltitude = Constants.mAltitude;
    private double mHeading = Constants.mHeading;
    private double mPitch = Constants.mPitch;
    private double mRoll = Constants.mRoll;

    private SceneView mSceneView;
    private View mMask;
    private MarkerLayout mMarkerLayout;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public EarthView(Context context) {
        this(context, null, 0);
    }

    public EarthView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EarthView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.earth_view, this);

        mSceneView = view.findViewById(R.id.sceneView);
        mMask = view.findViewById(R.id.mask);
//        setupMap();
    }

    public EarthView scene(Basemap.Type sceneType) {
        this.mScene = new ArcGISScene(sceneType);
        return this;
    }

    public EarthView scene(ArcGISScene scene) {
        this.mScene = scene;
        return this;
    }

    public EarthView latitude(double latitude) {
        this.mLatitude = latitude;
        return this;
    }

    public EarthView longitude(double longitude) {
        this.mLongitude = longitude;
        return this;
    }

    public EarthView altitude(double altitude) {
        this.mAltitude = altitude;
        return this;
    }

    public EarthView heading(double heading) {
        this.mHeading = heading;
        return this;
    }

    public EarthView pitch(double pitch) {
        this.mPitch = pitch;
        return this;
    }

    public EarthView roll(double roll) {
        this.mRoll = roll;
        return this;
    }

    public void init(Runnable onReady) {

        if (mScene == null)
            mScene = new ArcGISScene(Basemap.Type.IMAGERY);
        Camera camera = new Camera(mLatitude, mLongitude, mAltitude,
                mHeading, mPitch, mRoll);

        mSceneView.setScene(mScene);
        mSceneView.setViewpointCamera(camera);
        mMarkerLayout = new MarkerLayout(mContext, mSceneView);
        mSceneView.setOnTouchListener(new SceneViewOnTouchListener(mSceneView));
        mScene.addDoneLoadingListener(() ->
                mHandler.postDelayed(() -> {
                    mMask.setVisibility(View.GONE);
//                    mMarkerLayout.setOnMarkerTapListener(this);
                    if (onReady != null) onReady.run();
                }, 1000));
    }

    public void setOnMarkerTapListener(MarkerLayout.OnMarkerTapListener listener) {
        mMarkerLayout.setOnMarkerTapListener(listener);
    }

    public void setAdapter(@NotNull MarkerLayout.Adapter adapter) {
        mMarkerLayout.setAdapter(adapter);
    }

    public void flyToMarker(MarkerPoint hashPoint, Set<MarkerPoint> set) {
        Point point = mSceneView.getCurrentViewpointCamera().getLocation();

        double targetZ = Constants.mAltitudes[Constants.mAltitudes.length - 1];
        if (!ArrayUtils.isEmpty(set)) {
            targetZ = Utils.getTargetAltitude(point);
        }
        Point target = new Point(hashPoint.x, hashPoint.y, targetZ);
        Utils.moveMap(mSceneView, target, calcDuration(targetZ));
    }

    public void resetMap(Runnable onComplete) {
        Camera camera = new Camera(mLatitude, mLongitude, mAltitude,
                mHeading, mPitch, mRoll);
        Utils.moveMap(mSceneView, camera, calcDuration(mSceneView), onComplete, false);
    }

    private float calcDuration(@NotNull SceneView sceneView) {
        double altitude = sceneView.getCurrentViewpointCamera().getLocation().getZ();
        return calcDuration(altitude);
    }

    private float calcDuration(double targetAltitude) {
        float animationDuration = (float) (Math.abs((mAltitude - targetAltitude)
                / (mAltitude - Constants.mAltitudes[Constants.mAltitudes.length - 1])) * 2f);
        animationDuration = Math.max(Math.min(animationDuration, 2f), 0.5f);
        return animationDuration;
    }

    private class SceneViewOnTouchListener extends DefaultSceneViewOnTouchListener {

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
