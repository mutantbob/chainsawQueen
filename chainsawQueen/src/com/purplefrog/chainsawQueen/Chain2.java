package com.purplefrog.chainsawQueen;

import android.graphics.*;

/**
 * Logic for drawing the blade of the chainsaw pointing left.
 * It is the bottom chainsaw in the complete logo.
 */
public class Chain2
    extends ChainX
{
    private static final String LOG_TAG = Chain2.class.getName();

    public Chain2()
    {
        super(530, 1000-309, 196, 1000-414, 418, 1000-284, false);

        if (false) {
            x0 = 530;

            y0 = 1000-309;
            x4 = 418;
            y4 = 1000-284;
            cx = 196;
            cy = 1000-414;
        }
    }
}
