package com.purplefrog.chainsawQueen;

import android.graphics.*;
import android.os.*;
import android.service.wallpaper.*;
import android.view.*;

/**
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 12/17/12
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChainsawTeddy
    extends WallpaperService
{
    public final static String LOG_TAG = ChainsawTeddy.class.getName();

    Handler mHandler = new Handler();

    @Override
    public Engine onCreateEngine()
    {
        return new MyEngine();
    }

    public class MyEngine
        extends Engine
    {

        Runnable redrawtask = new Runnable()
        {
            public void run()
            {
                drawFrame();
            }
        };
        public boolean rememberedVisible=false;
        protected Chain1 chain1 = new Chain1();
        protected Chain2 chain2 = new Chain2();

        @Override
        public void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacks(redrawtask);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            rememberedVisible = visible;
            if (visible) {
                drawFrame();
            } else {
                mHandler.removeCallbacks(redrawtask);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            rememberedVisible = false;
            mHandler.removeCallbacks(redrawtask);
        }

        /*
    * Draw one frame of the animation. This method gets called repeatedly
    * by posting a delayed Runnable. You can do any drawing you want in
    * here. This example draws a wireframe cube.
    */
        void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();

            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                    // draw something
                   drawFrame_(c);
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }

            // Reschedule the next redraw
            mHandler.removeCallbacks(redrawtask);
            if (rememberedVisible) {
                mHandler.postDelayed(redrawtask, 1000 / 25);
            }

        }

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

            Matrix m2 = ChainsawQueen.matrixToCenterSVG(w, h);

            // skull logo
            double toothPeriod = 0.2; // seconds
            float phase = (float) ((System.currentTimeMillis() / 1000.0 / toothPeriod) % 1);
            chain2.drawBlade2(c, phase, m2);
            Picture.chainsaw_1_b(c, m2, new Paint());
            chain1.drawBlade1(c, phase, m2);
            Picture.chainsaw_1_a(c, m2, new Paint());

            Picture.teddy_head(c, m2, new Paint());
        }

        protected Point oldD = null;
        private void logDimensions(int w, int h)
        {
            if (oldD==null || w != oldD.x || h != oldD.y) {
//                Log.d(LOG_TAG, "wallpaper dimensions "+w+"x"+h);
                oldD = new Point(w,h);
            }
        }
    }

}
