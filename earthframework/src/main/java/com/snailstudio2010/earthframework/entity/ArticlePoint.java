package com.snailstudio2010.earthframework.entity;

import android.graphics.Bitmap;

import com.snailstudio2010.earthframework.MarkerPoint;

/**
 * Created by xuqiqiang on 2019/08/20.
 */
public class ArticlePoint extends MarkerPoint {

    public String info;
    public String photo;
    public Bitmap bitmap;

    public ArticlePoint(double x, double y, String info, String photo) {
        super(x, y);
        this.info = info;
        this.photo = photo;
    }

    @Override
    public String toString() {
        return "ArticlePoint{" +
                "info='" + info + '\'' +
                ", photo='" + photo + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}