package com.purplefrog.chainsawQueen;

import android.graphics.*;

/**
 * This is an animated wallpaper that draws a skull with chainsaws crossed beneath.
 *
 * The static art comes from svgs/skull.svg.
 *
 * The animated blades come from {@link Chain1} and {@link Chain2}
 *
 * <p> This project is based on the example CubeWallpaper1 from google
 * (since there is no proper documentation on how to write an animated wallpaper).
 */
public class ChainsawQueen
    extends BoringLiveWallpaper
{
    public final static String LOG_TAG = ChainsawQueen.class.getName();

    protected Chain1 chain1 = new Chain1();
    protected Chain2 chain2 = new Chain2();

    @Override
    public void drawFrame_(Canvas c)
    {
        int w = c.getWidth();
        int h = c.getHeight();

        logDimensions(w,h);

        {
            Paint p0 = new Paint();
            p0.setARGB(255, 0, 0, 0);
            p0.setStrokeWidth(0);

            c.drawRect(0, 0, w, h, p0);
        }

        Matrix m2 = matrixToCenterSVG(w, h);

        // skull logo
        double toothPeriod = 0.2; // seconds
        float phase = (float) ((System.currentTimeMillis() / 1000.0 / toothPeriod) % 1);
        chain2.drawBlade2(c, phase, m2);
        Picture.chainsaw_1_b(c, m2, new Paint());
        chain1.drawBlade1(c, phase, m2);
        Picture.chainsaw_1_a(c, m2, new Paint());

        Picture.skull(c, m2, new Paint());
    }


}
