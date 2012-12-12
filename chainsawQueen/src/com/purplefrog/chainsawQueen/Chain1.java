package com.purplefrog.chainsawQueen;

import android.graphics.*;
import android.util.*;

/**
 * Logic for drawing the blade of the chainsaw pointing right.
 * It is the top chainsaw in the complete logo.
 *
 */
public class Chain1
    extends ChainX
{

    public Chain1()
    {
        super();

        float[] theta_r = new float[1];

        //

        PointF v1 = solveCircles(x0, y0, cx, cy, pulleyRadius, true, theta_r);
        theta1 = theta_r[0];
//        Log.d(LOG_TAG, " v1 = "+v1.x+", "+v1.y);
        x1 = v1.x;
        y1 = v1.y;

        float v1x = x1 - x0;
        float v1y = y1 - y0;

        v1m = ChainsawQueen.magnitude(v1x, v1y);

        v1x_ = v1x / v1m;
        v1y_ = v1y / v1m;

        //

        PointF v3 = solveCircles(x4, y4, cx, cy, pulleyRadius, false, theta_r);
        theta2 = theta_r[0];

        x3 = v3.x;
        y3 = v3.y;
        float v3x = x4 - x3;
        float v3y = y4 - y3;
        v3m = ChainsawQueen.magnitude(v3x, v3y);
        v3x_ = v3x /v3m;
        v3y_ = v3y /v3m;
    }

    public void drawBlade1(Canvas c, float phase, Matrix m0)
    {

        drawBlade(c, phase, m0, true);
    }
}
