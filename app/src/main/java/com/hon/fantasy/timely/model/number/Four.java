package com.hon.fantasy.timely.model.number;

import com.hon.fantasy.timely.model.core.Figure;

/**
 * Created by Frank on 2018/3/4.
 * E-mail:frank_hon@foxmail.com
 */

public class Four extends Figure {
    private static final float[][] POINTS = {
            {0.856353591160221f, 0.806629834254144f}, {0.856353591160221f, 0.806629834254144f}, {0.237569060773481f, 0.806629834254144f},
            {0.237569060773481f, 0.806629834254144f}, {0.237569060773481f, 0.806629834254144f}, {0.712707182320442f, 0.138121546961326f},
            {0.712707182320442f, 0.138121546961326f}, {0.712707182320442f, 0.138121546961326f}, {0.712707182320442f, 0.806629834254144f},
            {0.712707182320442f, 0.806629834254144f}, {0.712707182320442f, 0.806629834254144f}, {0.712707182320442f, 0.988950276243094f},
            {0.712707182320442f, 0.988950276243094f}

    };

    private static Four INSTANCE = new Four();

    protected Four() {
        super(POINTS);
    }

    public static Four getInstance() {
        return INSTANCE;
    }
}
