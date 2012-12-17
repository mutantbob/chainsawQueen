package com.purplefrog.chainsawQueen;

import android.graphics.*;

/**
 * Code for drawing the rotating teeth on a chainsaw.
 * The fundamental shape of the chainsaw is teeth coming from one point (x0,y0)
 * travelling to a point (x1,y1) which is calculated as a tangent to
 * the circular pulley centered at (cx,cy) with radius {@link #pulleyRadius},
 * travelling around the pulley to point (x3,y3) which is calculated as another tangent
 * and finally travelling to (x4,y4).
 *
 * <p>The direction of travel for the teeth is configurable (since I need it to be different for each side).
 *
 * <p>The values for all the points are set (or computed) in the constructors for derived classes.</p>
 */
public class ChainX
{
    protected static final String LOG_TAG = Chain1.class.getName();
    /** chain start point*/
    float x0;
    float y0;
    /** upper circle tangent */
    float x1;
    float y1;
    /** bottom circle tangent*/
    float x3;
    float y3;
    /** lower chain terminus.*/
    float x4;
    float y4;
    /** pulley center*/
    float cx;
    float cy;
    protected final int pulleyRadius = 85;
    protected float v1m;
    /**
     * [v1x_,v1y_] is a length-1 direction vector pointing from [x0,y0] to [x1,y1]
     */
    protected float v1x_;
    protected float v1y_;
    protected float v3m;
    /**
     * [v3x_,v3y_] is a length-1 direction vector pointing from [x3,y3] to [x4,y4]
     */
    protected float v3x_;
    protected float v3y_;
    /**
     * angles from pulley center to tangents
     */
    protected float theta1;
    protected float theta2;
    float longTooth = 45;
    float valleyTooth = 15;

    public ChainX()
    {
        y4 = 1000-271;
        y0 = 671;
        cx = 767;
        x0 = 457;
        x4 = 482;
        cy = 1000-414;
    }

    public void drawBlade(Canvas c, float phase, Matrix m0, boolean clockwise)
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.FILL);

        paint.setARGB(255, 0, 0, 0);
        drawChain_(c, phase, m0, paint, 20, clockwise, false);

        paint.setARGB(255, 255, 255, 255);
        drawChain_(c, phase, m0, paint, 0, clockwise, true);

//        debugStuff(c, m0, clockwise);

    }

    public void drawChain_(Canvas c, float phase, Matrix m0, Paint paint, float extraToothDistance, boolean clockwise, boolean excludeInterior)
    {
        Path path = new Path();

        float toothPeriod = 35; // how "long" is a tooth on the chain
        {

            path.moveTo(x0,y0);

            float pulleyPartLength = Math.abs(pulleyChainSweepRadians(clockwise)) * (pulleyRadius+valleyTooth);
            for (int i=-3; (i/2-1)*toothPeriod<(v1m + pulleyPartLength + v3m); i++) {

                float t = toothPeriod * (i-1)/2
                    + phase *toothPeriod;

                boolean valley = 0 != i % 2;
                PointF w;
                float somethingTooth = extraToothDistance + (valley ? valleyTooth : longTooth);
                if (t<v1m) {
                    w = outgoingToothCoord(t, somethingTooth, clockwise);
                } else if (t<v1m+ pulleyPartLength){
                    w = pulleyToothCoord(t - v1m, somethingTooth + pulleyRadius, clockwise);
                } else {
                    w = lowerToothCoord(t-v1m-pulleyPartLength, somethingTooth, clockwise);
                }
                path.lineTo(w.x, w.y);
            }
            // path.lineTo(x1,y1);
        }

        path.lineTo(x4, y4);
        if (excludeInterior) {
            path.lineTo(x3, y3);

            path.arcTo(new RectF(cx-pulleyRadius, cy-pulleyRadius, cx+pulleyRadius, cy+pulleyRadius),
                (float)(theta2*180/Math.PI), (float)(-pulleyChainSweepRadians(clockwise) *180/Math.PI));
        }
        path.close();

        path.transform(m0);

        c.drawPath(path, paint);
    }

    private float pulleyChainSweepRadians(boolean clockwise)
    {
        float rval = theta2 - theta1;
        if (!clockwise)
            rval = -rval;

        if (rval<0)
            rval += 2*Math.PI;

        if (clockwise)
            return rval;
        else
            return -rval;
    }

    public PointF outgoingToothCoord(float t, float dist, boolean leftNotRight)
    {
        float x5 = x0 + t * v1x_ + (leftNotRight ? v1y_:-v1y_) * dist;
        float y5 = y0 + t * v1y_ +(leftNotRight ? - v1x_:v1x_) * dist;
        return new PointF(x5,y5);
    }

    public PointF lowerToothCoord(float t, float dist, boolean clockwise)
    {
        float x5 = x3 + t * v3x_ + (clockwise ? v3y_:-v3y_) * dist;
        float y5 = y3 + t * v3y_ +(clockwise ? - v3x_:v3x_) * dist;
        return new PointF(x5,y5);
    }

    public PointF pulleyToothCoord(float t, float pointDistance, boolean clockwise)
    {

        float radialFactor = pulleyRadius +valleyTooth;

        float theta = (clockwise ? t:-t) / radialFactor
            + theta1;

        float x5 = cx + (float) Math.cos(theta)* pointDistance;
        float y5 = cy + (float) Math.sin(theta)* pointDistance;
        return new PointF(x5, y5);
    }

    public static PointF solveCircles(float x0, float y0, float cx, float cy, float pulleyRadius, boolean leftNotRight, float[] theta_r)
    {
        float dx = x0-cx;
        float dy = y0-cy;

        float r0_ = magnitude(dx, dy);
        float r0 = r0_ /2;
        float r3 = r0;

        float x2 = xOfIntersectingCircles(pulleyRadius, r0, r3);
        float y2 = (float) Math.sqrt(pulleyRadius*pulleyRadius-x2*x2);

//        Log.d(LOG_TAG, " v2 = " + x2 + ", " + y2);

        float xpp = dx / r0_ * x2;
        float ypp = dy / r0_ * x2;
        float xp = - dy/r0_ * y2;
        float yp =   dx/r0_ * y2;

        if (!leftNotRight) {
            xp = -xp;
            yp = -yp;
        }

        {
            theta_r[0] = (float) Math.atan2(ypp+yp, xpp+xp);

//            Log.d(LOG_TAG, "theta = "+theta_r[0]+" = atan2("+xp+"+"+xpp+", " + yp + "+" + ypp + ")");
        }

        return new PointF(cx + xpp + xp, cy + ypp + yp);

    }

    /**
     * thank you <a href="http://mathworld.wolfram.com/Circle-CircleIntersection.html">MathWorld</a>
     *
     * @param r1 radius of circle at the origin
     * @param r2     radius of circle on the right
     * @param d x coordinate of circle on the right.
     * @return
     */
    public static float xOfIntersectingCircles(float r1, float r2, float d)
    {
        return (d*d - r2*r2 + r1*r1) / (2*d);
    }

    public static float magnitude(float dx, float dy)
    {
        return (float) Math.sqrt(dx*dx + dy*dy);
    }
}
