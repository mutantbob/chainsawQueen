package com.purplefrog.chainsawQueen;

/**
 * Created by thoth on 1/14/14.
 */
public class Point2DF
{
    public float x,y;

    public Point2DF(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public String toString()
    {
        return "<"+x+","+y+">";
    }
}
