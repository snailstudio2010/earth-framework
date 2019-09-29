package com.snailstudio2010.earthframework.demo;

import com.snailstudio2010.earthframework.MarkerPoint;

/**
 * Created by xuqiqiang on 2019/08/20.
 */
public class ArticlePoint extends MarkerPoint {

    public String info;

    public ArticlePoint(double x, double y, String info) {
        super(x, y);
        this.info = info;
    }

    @Override
    public String toString() {
        return "ArticlePoint{" +
                "info='" + info + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}