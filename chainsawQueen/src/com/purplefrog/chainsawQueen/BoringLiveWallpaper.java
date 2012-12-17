package com.purplefrog.chainsawQueen;

import android.graphics.*;
import android.os.*;
import android.service.wallpaper.*;
import android.view.*;

/**
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 12/17/12
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BoringLiveWallpaper
    extends WallpaperService
{
    Handler mHandler = new Handler();
    protected Point oldD = null;

    @Override
    public Engine onCreateEngine()
    {
        return new MyEngine();
    }

    public abstract void drawFrame_(Canvas c);

    protected void logDimensions(int w, int h)
    {
        if (oldD==null || w != oldD.x || h != oldD.y) {
//                Log.d(LOG_TAG, "wallpaper dimensions "+w+"x"+h);
            oldD = new Point(w,h);
        }
    }

    public static Matrix matrixToCenterSVG(int w, int h)
    {
        int d = Math.min(h, w);
        float shrink = d/1000.0f;
        Matrix m2 = new Matrix();
        m2.setConcat(
            translateMatrix((w - d) / 2.0f, (h - d) / 2.0f),
            scaleMatrix(shrink, shrink)
        );
        return m2;
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

        /**
         * Draw one frame of the animation. This method gets called repeatedly
         * by posting a delayed Runnable.
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

    }
}
