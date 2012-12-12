package com.purplefrog.chainsawQueen;

import android.graphics.*;
import android.os.*;
import android.service.wallpaper.*;
import android.util.*;
import android.view.*;

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

    public static Matrix rotateMatrix(int px, int py, int degrees)
    {
        Matrix degrees45 = new Matrix();
        degrees45.setRotate(degrees, px, py);
        return degrees45;
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
            if (true) {
                Matrix m2 = matrixToCenterSVG(w, h);
                if (false) {
                    Picture.svg_document(c, m2, new Paint());
                } else {
//                    Picture.Layer(c, m2, new Paint());
                    Picture.skull(c, m2, new Paint());
                    Picture.Layer_1(c, m2, new Paint());

                    int toothRate = 6; // how fast do teeth move as time passes
                    float phase = (this.timeCounter % toothRate) / (float) toothRate;
                    chain2.drawBlade2(c, phase, m2);
                    Picture.chainsaw_1_b(c, m2, new Paint());
                    chain1.drawBlade1(c, phase, m2);
                    Picture.chainsaw_1_a(c, m2, new Paint());

                }
            }
/*

            // rectangles
            if (false) {
                Matrix m0 = new Matrix();
                if (true) {
                    int d = Math.min(h, w);
                    float shrink = d /1000.0f;
                    m0.setScale(shrink, -shrink);
                    m0.setTranslate((w-d)/2.0f, (h-d)/2.0f);
                }
                Picture2.svg_document(c, m0, new Paint());
            }

            if (false) {
                // testing our arc transformation logic
                Matrix m2 = new Matrix();
                m2.reset();
                Picture3.svg_document(c, m2, new Paint());
            }
*/

            if (false)
                crazyArcTest(c);

            if (false) {

                Paint paint = new Paint();
                paint.setARGB(200, 255, 255,80);
                paint.setStrokeWidth(5);

                Path p = androidPathBug();

                c.drawPath(p, paint);
            }

            // rotating rectangle
            if (false) {
                Paint paint = new Paint();
                paint.setARGB(200, 255, 80,80);
                paint.setStrokeWidth(5);


                Path path = squarePath(w, h);

                c.drawPath(path, paint);
            }


            if (false) {
                int toothRate = 6; // how fast do teeth move as time passes

                float phase = (this.timeCounter % toothRate) / (float) toothRate;
                chain1.drawBlade1(c, phase, this.matrixToCenterSVG(w, h));

                chain2.drawBlade2(c, phase, this.matrixToCenterSVG(w, h));
            }

        }

        private Matrix matrixToCenterSVG(int w, int h)
        {
            int d = Math.min(h, w);
            float shrink = d/1000.0f;
//                shrink = 1.0f;
            Matrix m2 = new Matrix();
            if (true) {
                m2.setConcat(
                    translateMatrix((w - d) / 2.0f, (h - d) / 2.0f),
                    scaleMatrix(shrink, shrink)
                );
            } else if (true) {
                m2.setConcat(
                    scaleMatrix(shrink, shrink),
                    translateMatrix((w - d) / 2.0f, (h - d) / 2.0f)
                );

            } else {
                m2 = scaleMatrix(shrink,shrink);
            }
            return m2;
        }

        private void crazyArcTest(Canvas c)
        {
            Paint g = new Paint();
            g.setARGB(255, 255, 0, 255);

            {
                Path p = new Path();

                int x=100; int y=100;
                p.moveTo(x,y);
                p.arcTo(new RectF(x, y, x+100, y+100), 0, 270);

                c.drawPath(p, g);
            }
            {
                Path p = new Path();

                int x=200; int y=100;
                p.moveTo(x,y);
                p.arcTo(new RectF(x, y, x+100, y+100), 30, 90);

                c.drawPath(p, g);
            }
            {
                Path p = new Path();

                int x=100; int y=200;
                p.moveTo(x,y);
                p.arcTo(new RectF(x, y, x+100, y+100), 120, 90);

                c.drawPath(p, g);
            }
            {
                Path p = new Path();

                int x=200; int y=200;
                p.moveTo(x,y);
                p.arcTo(new RectF(x, y, x+100, y+100), 210, 180);

                c.drawPath(p, g);
            }

            //

            {
                Path p = new Path();

                int x=100; int y=300;
                p.moveTo(x,y);
                p.arcTo(new RectF(x, y, x+50, y+100), 0, 270);

                c.drawPath(p, g);
            }
            {
                Path p = new Path();

                int x=150; int y=300;
                p.moveTo(x,y);
                p.arcTo(new RectF(x, y, x+50, y+100), 30, 90);

                c.drawPath(p, g);
            }   {
                Path p = new Path();

                int x=100; int y=400;
                p.moveTo(x,y);
                p.arcTo(new RectF(x, y, x+50, y+100), 120, 90);

                c.drawPath(p, g);
            }
            {
                Path p = new Path();

                int x=150; int y=400;
                p.moveTo(x,y);
                p.arcTo(new RectF(x, y, x+50, y+100), 210, 180);

                c.drawPath(p, g);
            }

            //

            // rotated 45
            {
                Path p = new Path();

                int x=250; int y=300;
                p.moveTo(x,y);
                p.transform(rotateMatrix(x + 25, y + 50, 45));
                p.arcTo(new RectF(x, y, x + 50, y + 100), 0, 270);
                p.transform(rotateMatrix(x + 25, y + 50, -45));

                c.drawPath(p, g);
            }
            {
                Path p = new Path();

                int x=300; int y=300;
                p.moveTo(x,y);
                p.transform(rotateMatrix(x + 25, y + 50, 45));
                p.arcTo(new RectF(x, y, x + 50, y + 100), 30, 90);
                p.transform(rotateMatrix(x + 25, y + 50, -45));

                c.drawPath(p, g);
            }
            {
                Path p = new Path();

                int x=250; int y=400;
                p.moveTo(x,y);
                p.transform(rotateMatrix(x + 25, y + 50, 45));
                p.arcTo(new RectF(x, y, x + 50, y + 100), 120, 90);
                p.transform(rotateMatrix(x + 25, y + 50, -45));

                c.drawPath(p, g);
            }
            {
                Path p = new Path();

                int x=300; int y=400;
                p.moveTo(x,y);
                p.transform(rotateMatrix(x + 25, y + 50, 45));
                p.arcTo(new RectF(x, y, x + 50, y + 100), 210, -180);
                p.transform(rotateMatrix(x + 25, y + 50, -45));

                c.drawPath(p, g);
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


        private Path squarePath(int w, int h)
        {
            int cx = w/2;
            int cy = h/2;
            int r = Math.min(w,h)/3;

            double theta = (timeCounter)*3.14159 * 3/180;

            float[] pts = new float[2*4];

            pts[0] = (float) (cx + r*Math.cos(theta));
            pts[1] = (float) (cy + r*Math.sin(theta));

            pts[2] = (float) (cx - r*Math.sin(theta));
            pts[3] = (float) (cy + r*Math.cos(theta));

            pts[4] = (float) (cx - r*Math.cos(theta));
            pts[5] = (float) (cy - r*Math.sin(theta));

            pts[6] = (float) (cx + r*Math.sin(theta));
            pts[7] = (float) (cy - r*Math.cos(theta));

//                pts[8] = pts[0];
//                pts[9] = pts[1];

            Path path = new Path();
            path.moveTo(pts[0], pts[1]);
            for (int i=2; i<pts.length; i+=2) {
                path.lineTo(pts[i], pts[i+1]);
            }
            path.close();
            return path;
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
