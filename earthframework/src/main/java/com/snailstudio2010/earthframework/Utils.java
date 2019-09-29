/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Earth Framework
 */
package com.snailstudio2010.earthframework;

import android.util.Log;

import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;

/**
 * Created by xuqiqiang on 2019/08/19.
 */
public final class Utils {
    public static final String TAG = "earth-framework";

    private Utils() {
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
}
