package com.purplefrog.chainsawQueen;

import android.graphics.*;

/**
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 12/17/12
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChainsawTeddy
    extends BoringLiveWallpaper
{
    Chain1 chain1 = new Chain1();
    Chain2 chain2 = new Chain2();

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

        Matrix m2 = BoringLiveWallpaper.matrixToCenterSVG(w, h);

        // skull logo
        double toothPeriod = 0.2; // seconds
        float phase = (float) ((System.currentTimeMillis() / 1000.0 / toothPeriod) % 1);

        chain2.drawBlade(c, phase, m2);
        Picture.chainsaw_1_b(c, m2, new Paint());

        chain1.drawBlade(c, phase, m2);
        Picture.chainsaw_1_a(c, m2, new Paint());

        Picture.teddy_head(c, m2, new Paint());
        Picture.teddy_ears(c, m2, new Paint());
    }

}
