/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Earth Framework
 */
package com.snailstudio2010.earthframework;

import android.util.Log;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.snailstudio2010.libutils.NotNull;

/**
 * Created by xuqiqiang on 2019/08/19.
 */
public final class EarthUtils {
    public static final String TAG = "earth-framework";

    private EarthUtils() {
    }

    public static void addGraphic(GraphicsOverlay graphicsOverlay, Graphic graphic) {
        if (graphicsOverlay != null
                && graphicsOverlay.getGraphics() != null
                && graphic != null) {
            try {
                graphicsOverlay.getGraphics().add(graphic);
            } catch (Exception e) {
                e.printStackTrace();
                logE("ArcGISRuntimeException: Out of range");
            }
        }
    }

    public static void removeGraphic(GraphicsOverlay graphicsOverlay, Graphic graphic) {
        if (graphicsOverlay != null
                && graphicsOverlay.getGraphics() != null
                && graphic != null) {
            try {
                graphicsOverlay.getGraphics().remove(graphic);
            } catch (Exception e) {
                e.printStackTrace();
                logE("java.util.NoSuchElementException");
            }
        }
    }

    public static void logD(String msg) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, msg);
    }

    public static void logE(String msg) {
        if (BuildConfig.DEBUG)
            Log.e(TAG, msg);
    }

    public static void moveMap(SceneView sceneView, Camera camera, float animationDuration) {
        moveMap(sceneView, camera, animationDuration, null, false);
    }

    public static void moveMap(SceneView sceneView, Camera camera, float animationDuration, Runnable runnable, boolean strict) {

        ListenableFuture<Boolean> listenableFuture = sceneView.setViewpointCameraAsync(
                camera, animationDuration);
        if (runnable != null) {
            listenableFuture.addDoneListener(() -> {
                if (!strict) runnable.run();
                else {
                    Point point = sceneView.getCurrentViewpointCamera().getLocation();
                    if (point.getZ() < camera.getLocation().getZ() + 100 && point.getZ() > camera.getLocation().getZ() - 100) {
                        runnable.run();
                    }
                }
            });
        }
    }

    public static void moveMap(SceneView sceneView, Point target, float animationDuration) {
        moveMap(sceneView, target, animationDuration, null, false);
    }

    public static void moveMap(SceneView sceneView, Point target, float animationDuration, Runnable runnable, boolean strict) {
        Camera camera = sceneView.getCurrentViewpointCamera();
        moveMap(sceneView, camera.moveTo(target), animationDuration, runnable, strict);
    }

    public static double getTargetAltitude(Point point) {
        for (double item : Constants.mAltitudes) {
            if (point.getZ() > item + 100) return item;
        }
        return Constants.mAltitudes[Constants.mAltitudes.length - 1];
    }
}
