package com.hon.fantasy.timely.model;

import com.hon.fantasy.timely.model.number.Eight;
import com.hon.fantasy.timely.model.number.Five;
import com.hon.fantasy.timely.model.number.Four;
import com.hon.fantasy.timely.model.number.Nine;
import com.hon.fantasy.timely.model.number.Null;
import com.hon.fantasy.timely.model.number.One;
import com.hon.fantasy.timely.model.number.Seven;
import com.hon.fantasy.timely.model.number.Six;
import com.hon.fantasy.timely.model.number.Three;
import com.hon.fantasy.timely.model.number.Two;
import com.hon.fantasy.timely.model.number.Zero;

import java.security.InvalidParameterException;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public class NumberUtils {

    public static float[][] getControlPointsFor(int start) {
        switch (start) {
            case (-1):
                return Null.getInstance().getControlPoints();
            case 0:
                return Zero.getInstance().getControlPoints();
            case 1:
                return One.getInstance().getControlPoints();
            case 2:
                return Two.getInstance().getControlPoints();
            case 3:
                return Three.getInstance().getControlPoints();
            case 4:
                return Four.getInstance().getControlPoints();
            case 5:
                return Five.getInstance().getControlPoints();
            case 6:
                return Six.getInstance().getControlPoints();
            case 7:
                return Seven.getInstance().getControlPoints();
            case 8:
                return Eight.getInstance().getControlPoints();
            case 9:
                return Nine.getInstance().getControlPoints();
            default:
                throw new InvalidParameterException("Unsupported number requested");
        }
    }
}

