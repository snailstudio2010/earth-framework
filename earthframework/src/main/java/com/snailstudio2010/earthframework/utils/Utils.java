package com.snailstudio2010.earthframework.utils;

public class Utils {

    static {
        System.loadLibrary("parabola");
    }

    public static native String parabola(double[] startPoint, double[] endPoint);
}