package com.snailstudio2010.earthframework;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.snailstudio2010.earthframework.entity.ArticlePoint;
import com.snailstudio2010.earthframework.gallery.GalleryView;
import com.snailstudio2010.earthframework.listener.EarthViewListener;
import com.snailstudio2010.earthframework.utils.Constants;
import com.snailstudio2010.earthframework.utils.EarthUtils;
import com.snailstudio2010.earthframework.utils.GPSUtils;
import com.snailstudio2010.libutils.ArrayUtils;
import com.snailstudio2010.libutils.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EarthView extends RelativeLayout implements GalleryView.OnGalleryListener, AMapLocationListener {

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
    private GalleryView mGalleryView;
    private AMapLocationClient locationClient;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean mLocationShowFlag;
    private boolean mLocationFlyTo;
    private boolean mLocationUseCompass;
    private AMapLocationListener mLocationListener;

    private List<EarthViewListener> mListeners = new ArrayList<>();

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
        mGalleryView = view.findViewById(R.id.gallery_view);
        mGalleryView.setOnGalleryListener(this);
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
        mSceneView.setOnTouchListener(new EarthViewOnTouchListener(mSceneView));
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

    public void flyToMarker(MarkerPoint hashPoint, Set<MarkerPoint> set, boolean showInfo) {
        Point point = mSceneView.getCurrentViewpointCamera().getLocation();

        double targetZ = Constants.mAltitudes[Constants.mAltitudes.length - 1];
        if (!ArrayUtils.isEmpty(set)) {
            targetZ = EarthUtils.getTargetAltitude(point);
        }
        Point target = new Point(hashPoint.x, hashPoint.y, targetZ);
        EarthUtils.moveMap(mSceneView, target, calcDuration(targetZ));

        if (showInfo) {
            List<ArticlePoint> list = ArrayUtils.createList(((ArticlePoint) hashPoint));
            list.addAll((List<ArticlePoint>) new ArrayList(set));
            mGalleryView.show(list);
//            mGalleryView.show(ArrayUtils.createList(((ArticlePoint) hashPoint)));
        }

    }

    public void resetMap(Runnable onComplete) {
        Camera camera = new Camera(mLatitude, mLongitude, mAltitude,
                mHeading, mPitch, mRoll);
        EarthUtils.moveMap(mSceneView, camera, calcDuration(mSceneView), onComplete, false);
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

    public SceneView getSceneView() {
        return mSceneView;
    }

    public void addListener(EarthViewListener listener) {
        if (!mListeners.contains(listener))
            mListeners.add(listener);
    }

    public void removeListener(EarthViewListener listener) {
        mListeners.remove(listener);
    }

    public void removeAllListener() {
        mListeners.clear();
    }

    @Override
    public void onGalleryItemSelect(int position, ArticlePoint articleItem) {
        mMarkerLayout.createSearchLocationGraphic(articleItem.x, articleItem.y);
        Point target = new Point(articleItem.x, articleItem.y,
                mSceneView.getCurrentViewpointCamera().getLocation().getZ());
        EarthUtils.moveMap(mSceneView, target, Constants.mFlyToPeriod);
    }

    @Override
    public void onGalleryItemClick(int position, ArticlePoint articleItem) {
    }

    @Override
    public void onGalleryClose() {
        mGalleryView.hide();
        mMarkerLayout.removeSearchLocationGraphic();
        resetMap(null);
    }

    private void initAMapLocation() {
        locationClient = new AMapLocationClient(mContext);
        AMapLocationClientOption locationOption = getDefaultOption();
        locationClient.setLocationOption(locationOption);
        locationClient.setLocationListener(this);
    }

    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mOption.setGpsFirst(false);
        mOption.setHttpTimeOut(30000);
        mOption.setInterval(2000);
        mOption.setNeedAddress(true);
        mOption.setOnceLocation(true);
        mOption.setOnceLocationLatest(false);
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);
        mOption.setSensorEnable(false);
        mOption.setWifiScan(true);
        mOption.setLocationCacheEnable(true);
        mOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.DEFAULT);
        return mOption;
    }

    public boolean startLocation(boolean showFlag, boolean flyTo, boolean useCompass, AMapLocationListener listener) {
        int requestPermissionsCode = 2;
        String[] requestPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (!(ContextCompat.checkSelfPermission(mContext, requestPermissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mContext, requestPermissions[1]) == PackageManager.PERMISSION_GRANTED)) {
            if (mContext instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) mContext, requestPermissions, requestPermissionsCode);
            }
            return false;
        } else {
            if (locationClient == null) {
                initAMapLocation();
            }
            mLocationShowFlag = showFlag;
            mLocationFlyTo = flyTo;
            mLocationUseCompass = useCompass;
            mLocationListener = listener;
            locationClient.startLocation();
        }
        return true;
    }

    @Override
    public void onLocationChanged(AMapLocation location) {
        if (null != location) {

            if (location.getErrorCode() == 0) {

                double[] point = GPSUtils.gcj02_To_Gps84(location.getLatitude(), location.getLongitude());
                location.setLatitude(point[0]);
                location.setLongitude(point[1]);

                if (mLocationFlyTo) {
                    resetMap(() -> {
                        double targetAltitude = Constants.mAltitudes[Constants.mAltitudes.length - 2];
                        Point target = new Point(location.getLongitude(), location.getLatitude(), targetAltitude);
                        EarthUtils.moveMap(mSceneView, target, calcDuration(targetAltitude), () -> {
                            if (mLocationShowFlag)
                                mMarkerLayout.createLocationGraphic(location, mLocationUseCompass);
                        }, false);
                    });
                } else if (mLocationShowFlag) {
                    mMarkerLayout.createLocationGraphic(location, mLocationUseCompass);
                }
            } else {
                Toast.makeText(mContext, location.getLocationDetail(), Toast.LENGTH_SHORT).show();
                if (location.getErrorCode() == 12) {
                    new AlertDialog.Builder(mContext)
                            .setMessage("Would you mind to turn GPS on?")
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok, (dialog, which)
                                    -> mContext
                                    .startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                            .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                            .create().show();
                }
            }
        }

        if (mLocationListener != null)
            mLocationListener.onLocationChanged(location);
    }

    private class EarthViewOnTouchListener extends DefaultSceneViewOnTouchListener {

        public EarthViewOnTouchListener(SceneView sceneView) {
            super(sceneView);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (mMarkerLayout != null)
                mMarkerLayout.onTouch(motionEvent);
            for (EarthViewListener listener : mListeners) {
                listener.onTouch(view, motionEvent);
            }
            return super.onTouch(view, motionEvent);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mMarkerLayout != null && mMarkerLayout.onSingleTap(e))
                return super.onSingleTapConfirmed(e);
            for (EarthViewListener listener : mListeners) {
                listener.onSingleTapConfirmed(e);
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            for (EarthViewListener listener : mListeners) {
                listener.onScale(scaleGestureDetector);
            }
            return super.onScale(scaleGestureDetector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            if (mMarkerLayout != null)
                mMarkerLayout.onScaleEnd();
            for (EarthViewListener listener : mListeners) {
                listener.onScaleEnd(scaleGestureDetector);
            }
            super.onScaleEnd(scaleGestureDetector);
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            for (EarthViewListener listener : mListeners) {
                listener.onScaleBegin(scaleGestureDetector);
            }
            return super.onScaleBegin(scaleGestureDetector);
        }


        public boolean onMultiPointerTap(MotionEvent motionEvent) {
            for (EarthViewListener listener : mListeners) {
                listener.onMultiPointerTap(motionEvent);
            }
            return super.onMultiPointerTap(motionEvent);
        }

        public boolean onDoubleTouchDrag(MotionEvent motionEvent) {
            for (EarthViewListener listener : mListeners) {
                listener.onDoubleTouchDrag(motionEvent);
            }
            return super.onDoubleTouchDrag(motionEvent);
        }

        public boolean onSinglePointerDown(MotionEvent motionEvent) {
            for (EarthViewListener listener : mListeners) {
                listener.onSinglePointerDown(motionEvent);
            }
            return super.onSinglePointerDown(motionEvent);
        }

        public boolean onSinglePointerUp(MotionEvent motionEvent) {
            for (EarthViewListener listener : mListeners) {
                listener.onSinglePointerUp(motionEvent);
            }
            return super.onSinglePointerUp(motionEvent);
        }

        public boolean onTwoPointerPitch(MotionEvent motionEvent, double pitchDelta) {
            for (EarthViewListener listener : mListeners) {
                listener.onTwoPointerPitch(motionEvent, pitchDelta);
            }
            return super.onTwoPointerPitch(motionEvent, pitchDelta);
        }

        public boolean onTwoPointerRotate(MotionEvent motionEvent, double rotationDelta) {
            for (EarthViewListener listener : mListeners) {
                listener.onTwoPointerRotate(motionEvent, rotationDelta);
            }
            return super.onTwoPointerRotate(motionEvent, rotationDelta);
        }

        public boolean onDoubleTap(MotionEvent motionEvent) {
            for (EarthViewListener listener : mListeners) {
                listener.onDoubleTap(motionEvent);
            }
            return super.onDoubleTap(motionEvent);
        }

        public boolean onSingleTapUp(MotionEvent motionEvent) {
            for (EarthViewListener listener : mListeners) {
                listener.onSingleTapUp(motionEvent);
            }
            return super.onSingleTapUp(motionEvent);
        }

        public boolean onScroll(MotionEvent motionEventFrom, MotionEvent motionEventTo, float distanceX, float distanceY) {
            for (EarthViewListener listener : mListeners) {
                listener.onScroll(motionEventFrom, motionEventTo, distanceX, distanceY);
            }
            return super.onScroll(motionEventFrom, motionEventTo, distanceX, distanceY);
        }

        public void onLongPress(MotionEvent motionEvent) {
            for (EarthViewListener listener : mListeners) {
                listener.onLongPress(motionEvent);
            }
            super.onLongPress(motionEvent);
        }

        public boolean onFling() {
            for (EarthViewListener listener : mListeners) {
                listener.onFling();
            }
            return super.onFling();
        }

    }
}
