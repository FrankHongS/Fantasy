package com.hon.fantasy.timely.model.number;

import com.hon.fantasy.timely.model.core.Figure;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public class Null extends Figure {
    private static final float[][] POINTS = {
            {0.5f, 0.5f}, {0.5f, 0.5f}, {0.5f, 0.5f},
            {0.5f, 0.5f}, {0.5f, 0.5f}, {0.5f, 0.5f},
            {0.5f, 0.5f}, {0.5f, 0.5f}, {0.5f, 0.5f},
            {0.5f, 0.5f}, {0.5f, 0.5f}, {0.5f, 0.5f},
            {0.5f, 0.5f}
    };

    private static final Null INSTANCE = new Null();

    protected Null() {
        super(POINTS);
    }

    public static Null getInstance() {
        return INSTANCE;
    }
}
