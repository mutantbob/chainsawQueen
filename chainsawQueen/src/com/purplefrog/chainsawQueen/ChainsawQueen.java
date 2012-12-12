package com.purplefrog.chainsawQueen;

import android.graphics.*;
import android.os.*;
import android.service.wallpaper.*;
import android.util.*;
import android.view.*;

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
    extends WallpaperService
{
    public final static String LOG_TAG = ChainsawQueen.class.getName();

    Handler mHandler = new Handler();

    @Override
    public Engine onCreateEngine()
    {
        return new MyEngine();
    }

    public static Matrix scaleMatrix(float sx, float sy)
    {
        Matrix m0 = new Matrix();
        m0.setScale(sx, sy);
        return m0;
    }

    public static Matrix translateMatrix(float dx, float dy)
    {
        Matrix m0 = new Matrix();
        m0.setTranslate(dx, dy);
        return m0;
    }

    private class MyEngine
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
        private int timeCounter=0;
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

            timeCounter++;
        }

        private void drawFrame_(Canvas c)
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

            // skull logo
            Matrix m2 = matrixToCenterSVG(w, h);

            Picture.skull(c, m2, new Paint());
            Picture.Layer_1(c, m2, new Paint());

            int toothRate = 6; // how fast do teeth move as time passes
            float phase = (this.timeCounter % toothRate) / (float) toothRate;
            chain2.drawBlade2(c, phase, m2);
            Picture.chainsaw_1_b(c, m2, new Paint());
            chain1.drawBlade1(c, phase, m2);
            Picture.chainsaw_1_a(c, m2, new Paint());

        }

        private Matrix matrixToCenterSVG(int w, int h)
        {
            int d = Math.min(h, w);
            float shrink = d/1000.0f;
//                shrink = 1.0f;
            Matrix m2 = new Matrix();
            m2.setConcat(
                translateMatrix((w - d) / 2.0f, (h - d) / 2.0f),
                scaleMatrix(shrink, shrink)
            );
            return m2;
        }

        /**
         * https://code.google.com/p/android/issues/detail?can=2&start=0&num=100&q=&colspec=ID%20Type%20Status%20Owner%20Summary%20Stars&groupby=&sort=&id=41216
         */
        private Path androidPathBug()
        {
            Path p = new Path();
            p.moveTo(100,100);
            p.rLineTo(100,0);
            p.rLineTo(0, 100);
            p.rLineTo(-100,20);
            p.close();

            if (false)
                p.moveTo(100,100);

            p.rLineTo(-50,50);
            p.rLineTo(-50, -50);
            p.rLineTo(50, -50);
            p.close();
            return p;
        }

        protected Point oldD = null;
        private void logDimensions(int w, int h)
        {
            if (oldD==null || w != oldD.x || h != oldD.y) {
                Log.d(LOG_TAG, "wallpaper dimensions "+w+"x"+h);
                oldD = new Point(w,h);
            }
        }
    }

    public static float magnitude(float dx, float dy)
    {
        return (float) Math.sqrt(dx*dx + dy*dy);
    }


}
