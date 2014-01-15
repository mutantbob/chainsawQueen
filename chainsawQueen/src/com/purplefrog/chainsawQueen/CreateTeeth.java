package com.purplefrog.chainsawQueen;

import java.io.*;

/**
 * This class uses the code from {@link ChainX} to fabricate SVG
 * that matches the teeth generated by the animated wallpaper.
 * Created by thoth on 1/14/14.
 */
public class CreateTeeth
{
    public static void main(String[] argv)
    {
        float toothPeriod=35;
        String duration = "0.25s";

        Chain1 c1 = new Chain1();

        animatedToothPath(c1, toothPeriod, duration, 20, false, "fill:#000");
        animatedToothPath(c1, toothPeriod, duration, 0, true, "fill:#fff");
        Chain2 c2 = new Chain2();

        animatedToothPath(c2, toothPeriod, duration, 20, false, "fill:#000");
        animatedToothPath(c2, toothPeriod, duration, 0, true, "fill:#fff");
    }

    private static void animatedToothPath(ChainX c1, float toothPeriod, String duration, float extraRadius, boolean excludeInterior, String style)
    {
        StringBuilder d = chainToothPath(toothPeriod, c1, (float) 0, extraRadius, excludeInterior);

        int frames = 8;
        StringBuilder values = new StringBuilder();
        for (int i=0; i<=frames; i++) {
            if (i>0)
                values.append(";\n");

            values.append(chainToothPath(toothPeriod, c1, toothPeriod* i/(float)frames, extraRadius, excludeInterior));
        }

        System.out.println("<path style=\"" + style + "\" d=\"" + d+"\">");
        System.out.println("<animate attributeName=\"d\" attributeType=\"XML\" dur=\""+duration+"\"  repeatCount=\"indefinite\"\n" +
            "values=\""+values+"\"/>");
        System.out.println("</path>");
    }

    public static StringBuilder chainToothPath(float toothPeriod, ChainX c1, float phase, float extraRadius, boolean excludeInterior)
    {
        StringBuilder rval = new StringBuilder("M ");
        float pulleyPartLength = pulleyPartLength(c1);
        for (int i=-4; ((i-1)/2.0f)*toothPeriod < c1.v1m+ pulleyPartLength+c1.v3m; i++) {
            float t = phase + i/2.0f*toothPeriod;

            boolean valley = i%2==0;
            float r1 =  (valley ? c1.valleyTooth : c1.longTooth) + extraRadius;
            Point2DF xy;
            if (t<c1.v1m) {
                xy = c1.outgoingToothCoord(t, r1, c1.clockwise);
            } else if (t-c1.v1m < pulleyPartLength) {
                xy = c1.pulleyToothCoord(t-c1.v1m, c1.pulleyRadius +r1, c1.clockwise);
            } else {
                xy = c1.lowerToothCoord(t-c1.v1m-pulleyPartLength, r1, c1.clockwise);
            }
            rval.append(xy.x+","+xy.y+" ");
        }

        if (excludeInterior) {
            rval.append(c1.x4+","+c1.y4+" "+
                c1.x3+","+c1.y3+" A "+c1.pulleyRadius+","+c1.pulleyRadius+" 0 1 "+(c1.clockwise?0:1)+" "+c1.x1+","+c1.y1+
                " L "+c1.x0+","+c1.y0);
        }

        rval.append(" Z");
        return rval;
    }

    public static void version1(float toothPeriod, String duration, ChainX c1, PrintStream out)
    {
        int pulleyRadius = c1.pulleyRadius;
        float valleyTooth = c1.valleyTooth;
        float longTooth = c1.longTooth;

        int toothShadow = 20;

        {
            out.println("<defs>\n");
            float r2 = pulleyRadius + longTooth+toothShadow;
            double x1 = c1.cx+ Math.cos(c1.theta1)* r2;
            double y1 = c1.cy+ Math.sin(c1.theta1)* r2;
            double x2 = c1.cx+ Math.cos(c1.theta2)* r2;
            double y2 = c1.cy+ Math.sin(c1.theta2)* r2;

            {
                float y3 = c1.cy - r2;
                float y4 = c1.cy + r2;
                float x4 = c1.cx + r2;

                String d2 = c1.cx+","+c1.cy+" "+
                    x1+","+y1+" "+
                    x1+","+ y3 +" "+
                    x4+","+y3+" "+
                    x4+","+y4+" "+
                    x2+","+y4+" "+
                    x2+","+y2+" Z"
                    ;
                out.println("<clipPath id=\"clipChain1\">\n" +
                    "<path d=\"M " + d2 + "\"/>\n" +
                    "</clipPath>");
            }

            {
                float x5 = Math.min(c1.x0,c1.x4) - r2;
                float y5 = Math.min( c1.cy-r2, Math.min(c1.y0, c1.y4)- (c1.longTooth+toothShadow));
                float y6 = Math.max(c1.cy+r2, Math.max(c1.y0, c1.y4)+ (c1.longTooth+toothShadow));


                String d3 = c1.cx+","+c1.cy+" "+
                    x2+","+y2+" "+
                    x2+","+y6+" "+
                    x5+","+y6+" "+
                    x5+","+y5+" "+
                    x1+","+y5+" "+
                    x1+","+y1+" Z";
                out.println("<clipPath id=\"clipChain2\">\n" +
                    "<path d=\"M " + d3 + "\"/>\n" +
                    "</clipPath>");
            }

            out.println("</defs>\n");
        }


        {
            out.println(
                "<g clip-path=\"url(#clipChain1)\">\n");
            {
                float phase = toothPeriod *(float) (1-fracPart(c1.v1m/toothPeriod));
                StringBuilder d = pathForPulleyTeeth(toothPeriod, c1, 0, phase, false);
                StringBuilder d2 = pathForPulleyTeeth(toothPeriod, c1, toothShadow, phase, true);

                double degrees = toothPeriod * 180 / (pulleyRadius+valleyTooth) / Math.PI;

                out.println(
                    "<g>\n" +
                        "<path style=\"fill:#000\" d=\"" + d2 + "\"/>\n" +
                        "<path style=\"fill:#fff\" d=\"" + d + "\"/>\n" +
                        "<animateTransform repeatCount=\"indefinite\"\n" +
                        "\t\t  dur=\"" + duration + "\"\n" +
                        "\t\t  attributeType=\"xml\"\n" +
                        "\t\t  attributeName=\"transform\"\n" +
                        "\t\t  type=\"rotate\"\n" +
                        "\t\t  from=\"0 " + c1.cx + " " + c1.cy + "\"\n" +
                        "\t\t  to=\"" + degrees + " " + c1.cx + " " + c1.cy + "\"/>" +
                        "</g>");
            }

            out.println("</g>");
        }

        {
            out.println("<g clip-path=\"#clipChain2\"> ");
            {
                StringBuilder d = outgoingToothPath(c1, toothPeriod, 0);
                StringBuilder d2 = outgoingToothPath(c1, toothPeriod, toothShadow);
                double dx = -toothPeriod * Math.sin(c1.theta1);
                double dy = toothPeriod * Math.cos(c1.theta1);

                out.println("<g>\n");
                out.println("<path style=\"fill:#000\" d=\"" + d2 + "\"/>");
                out.println("<path style=\"fill:#fff\" d=\"" + d + "\"/>");
                out.println("<animateTransform repeatCount=\"indefinite\" dur=\"" + duration + "\"" +
                    "\t\t  attributeType=\"xml\"\n" +
                    "\t\t  attributeName=\"transform\"\n" +
                    "\t\t  type=\"translate\"\n" +
                    "\t\t  from=\"0 0\"\n" +
                    "\t\t  to=\"" + dx + " " + dy + "\"\n" +
                    "/>");
                out.println("</g>");
            }
            {
                float phase_ = 1-(float)fracPart((c1.v1m + pulleyPartLength(c1)) / toothPeriod);
                float phase = phase_*toothPeriod;
                StringBuilder d = lowerToothPath(c1, toothPeriod, 0, phase);
                StringBuilder d2 = lowerToothPath(c1, toothPeriod, toothShadow, phase);
                double dx = -toothPeriod * Math.sin(c1.theta2);
                double dy = toothPeriod * Math.cos(c1.theta2);

                out.println("<g>\n");
                out.println("<path style=\"fill:#000\" d=\"" + d2 + "\"/>");
                out.println("<path style=\"fill:#fff\" d=\"" + d + "\"/>");
                out.println("<animateTransform repeatCount=\"indefinite\" dur=\"" + duration + "\"" +
                    "\t\t  attributeType=\"xml\"\n" +
                    "\t\t  attributeName=\"transform\"\n" +
                    "\t\t  type=\"translate\"\n" +
                    "\t\t  to=\"0 0\"\n" +
                    "\t\t  from=\"" + -dx + " " + -dy + "\"\n" +
                    "/>");
                out.println("</g>");
            }
            out.println(" </g>\n");
        }
    }

    public static double fracPart(float x)
    {
        return x - Math.floor(x);
    }

    public static StringBuilder outgoingToothPath(ChainX c1, float toothPeriod, int extraRadius)
    {
        StringBuilder d = new StringBuilder("M ");

        for (int i=-4; (i-2)/2.0*toothPeriod<c1.v1m; i++) {
            boolean valley = i%2==0;
            float r = extraRadius +(valley ? c1.valleyTooth:c1.longTooth);
            Point2DF xy = c1.outgoingToothCoord(i / 2.0f * toothPeriod, r, c1.clockwise);
            d.append(xy.x+","+xy.y+" ");
        }

        double x1 = c1.cx + Math.cos(c1.theta1)*c1.pulleyRadius;
        double y1 = c1.cy + Math.sin(c1.theta1)*c1.pulleyRadius;
        double x2 = c1.x0 + Math.sin(c1.theta1)*toothPeriod;
        double y2 = c1.y0 - Math.cos(c1.theta1)*toothPeriod;
        d.append(x1+","+y1+" "+x2+","+y2+" Z");
        return d;
    }

    public static StringBuilder lowerToothPath(ChainX c1, float toothPeriod, int extraRadius, float phase)
    {
        StringBuilder d = new StringBuilder("M ");

        for (int i=-4; (i-3)/2.0*toothPeriod<c1.v3m; i++) {
            boolean valley = i%2==0;
            float r = extraRadius +(valley ? c1.valleyTooth:c1.longTooth);
            Point2DF xy = c1.lowerToothCoord(phase + i / 2.0f * toothPeriod, r, c1.clockwise);
            d.append(xy.x+","+xy.y+" ");
        }

        double x2 = c1.cx + Math.cos(c1.theta2)*c1.pulleyRadius;
        double y2 = c1.cy + Math.sin(c1.theta2)*c1.pulleyRadius;
        double x1 = c1.x4 - Math.sin(c1.theta2)*toothPeriod;
        double y1 = c1.y4 + Math.cos(c1.theta2)*toothPeriod;
        d.append(x1+","+y1+" "+x2+","+y2+" Z");
        return d;
    }

    public static StringBuilder pathForPulleyTeeth(float toothPeriod, ChainX c1, int extraRadius, float phase, boolean excludeInterior)
    {


        int pulleyRadius = c1.pulleyRadius;
        float valleyTooth = c1.valleyTooth;
        float longTooth = c1.longTooth;

        float pulleyPartLength = pulleyPartLength(c1);

        StringBuilder d = new StringBuilder("M ");
        for (int i=-3; (i-2)*toothPeriod/2< pulleyPartLength; i++) {
            boolean valley = i%2==0;
            float r = extraRadius + pulleyRadius + (valley ? valleyTooth : longTooth);
            Point2DF x = c1.pulleyToothCoord(phase +(i/2.0f)*toothPeriod, r, c1.clockwise);
//            System.out.println(x);
            d.append(x.x+","+x.y+" ");
        }

        if (excludeInterior) {
            float theta2 = c1.theta2 + toothPeriod / pulleyRadius;
            float theta1 = c1.theta1 - toothPeriod / pulleyRadius;
            double x8 = c1.cx + Math.cos(theta2) * pulleyRadius;
            double y8 = c1.cy + Math.sin(theta2) * pulleyRadius;
            double x9 = c1.cx + Math.cos(theta1) * pulleyRadius;
            double y9 = c1.cy + Math.sin(theta1) * pulleyRadius;

            d.append("\n"+x8+","+y8);
            d.append("\nA "+pulleyRadius+","+pulleyRadius+" 0 1 "+(c1.clockwise?0:1)+" "+x9+","+y9);
        }
        return d;
    }

    public static float pulleyPartLength(ChainX c1)
    {
        return Math.abs(c1.pulleyChainSweepRadians(c1.clockwise)) * (c1.pulleyRadius + c1.valleyTooth);
    }
}
