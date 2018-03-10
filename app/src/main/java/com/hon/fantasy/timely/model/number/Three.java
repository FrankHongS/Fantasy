package com.hon.fantasy.timely.model.number;

import com.hon.fantasy.timely.model.core.Figure;

/**
 * Created by Frank on 2018/3/4.
 * E-mail:frank_hon@foxmail.com
 */

public class Three extends Figure {
    private static final float[][] POINTS = {
            {0.361878453038674f, 0.298342541436464f}, {0.348066298342541f, 0.149171270718232f}, {0.475138121546961f, 0.0994475138121547f},
            {0.549723756906077f, 0.0994475138121547f}, {0.861878453038674f, 0.0994475138121547f}, {0.806629834254144f, 0.530386740331492f},
            {0.549723756906077f, 0.530386740331492f}, {0.87292817679558f, 0.530386740331492f}, {0.828729281767956f, 0.994475138121547f},
            {0.552486187845304f, 0.994475138121547f}, {0.298342541436464f, 0.994475138121547f}, {0.30939226519337f, 0.828729281767956f},
            {0.312154696132597f, 0.790055248618785f}
    };

    private static Three INSTANCE = new Three();

    protected Three() {
        super(POINTS);
    }

    public static Three getInstance() {
        return INSTANCE;
    }
}
