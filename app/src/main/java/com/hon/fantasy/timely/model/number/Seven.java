package com.hon.fantasy.timely.model.number;

import com.hon.fantasy.timely.model.core.Figure;

/**
 * Created by Frank on 2018/3/4.
 * E-mail:frank_hon@foxmail.com
 */

public class Seven extends Figure {
    private static final float[][] POINTS = {
            {0.259668508287293f, 0.116022099447514f}, {0.259668508287293f, 0.116022099447514f}, {0.87292817679558f, 0.116022099447514f},
            {0.87292817679558f, 0.116022099447514f}, {0.87292817679558f, 0.116022099447514f}, {0.7f, 0.422099447513812f},
            {0.7f, 0.422099447513812f}, {0.7f, 0.422099447513812f}, {0.477348066298343f, 0.733149171270718f},
            {0.477348066298343f, 0.733149171270718f}, {0.477348066298343f, 0.733149171270718f}, {0.25414364640884f, 1f},
            {0.25414364640884f, 1f}
    };

    private static Seven INSTANCE = new Seven();

    protected Seven() {
        super(POINTS);
    }

    public static Seven getInstance() {
        return INSTANCE;
    }
}