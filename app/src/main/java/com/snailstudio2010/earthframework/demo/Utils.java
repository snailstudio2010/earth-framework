/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Earth Framework
 */
package com.snailstudio2010.earthframework.demo;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.snailstudio2010.libutils.NotNull;

import java.util.Random;

/**
 * Created by xuqiqiang on 2019/08/19.
 */
public final class Utils {

    private Utils() {
    }

    public static float calcDuration(@NotNull SceneView sceneView) {
        double altitude = sceneView.getCurrentViewpointCamera().getLocation().getZ();
        return calcDuration(altitude);
    }

    public static float calcDuration(double targetAltitude) {
        float animationDuration = (float) (Math.abs((Constants.mAltitude - targetAltitude)
                / (Constants.mAltitude - Constants.mAltitudes[Constants.mAltitudes.length - 1])) * 2f);
        animationDuration = Math.max(Math.min(animationDuration, 2f), 0.5f);
        return animationDuration;
    }

    public static void resetMap(SceneView sceneView, Runnable runnable) {
        Camera camera = new Camera(Constants.mLatitude, Constants.mLongitude, Constants.mAltitude,
                Constants.mHeading, Constants.mPitch, Constants.mRoll);
        moveMap(sceneView, camera, Utils.calcDuration(sceneView), runnable, false);
    }

    public static void moveMap(SceneView sceneView, Camera camera, float animationDuration, Runnable runnable, boolean strict) {
//        this.camera = camera;
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
        ListenableFuture<Boolean> listenableFuture = sceneView.setViewpointCameraAsync(
                camera.moveTo(target), animationDuration);
        if (runnable != null) {
            listenableFuture.addDoneListener(() -> {
                if (!strict) runnable.run();
                else {
                    Point point = sceneView.getCurrentViewpointCamera().getLocation();
                    if (point.getZ() < target.getZ() + 100 && point.getZ() > target.getZ() - 100) {
                        runnable.run();
                    }
                }
            });
        }
    }

    public static double getTargetAltitude(Point point) {
        for (double item : Constants.mAltitudes) {
            if (point.getZ() > item + 100) return item;
        }
        return Constants.mAltitudes[Constants.mAltitudes.length - 1];
    }
}
