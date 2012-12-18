package com.purplefrog.svgToAndroid;

import org.apache.log4j.*;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 12/17/12
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class DrawStyleParts
{
    private static final Logger logger = Logger.getLogger(DrawStyleParts.class);

    public String common;
    public String fillOnly;
    public String strokeOnly;

    public DrawStyleParts(CharSequence strokeOnly, CharSequence fillOnly)
    {
        this.common = null;
        this.strokeOnly = strokeOnly ==null ? null : strokeOnly.toString();
        this.fillOnly = fillOnly == null ? null : fillOnly.toString();
    }

    public static DrawStyleParts parseStyle(String style, String paintObjectName)
    {
        Color fill = null;
        Double fillOpacity = null;
        Color stroke = null;
        Double strokeOpacity=null;

        Double strokeWidth=null;

        for (String part : style.split("\\s*;\\s*")) {
            int idx = part.indexOf(':');
            if (idx<0) {
                throw new IllegalArgumentException("bare attribute in @style '"+part+"'");
            }

            String key = part.substring(0, idx);
            String value = part.substring(idx+1);

            if ("fill".equals(key)) {
                fill = updateRGB(value);
            } else if ("stroke".equals(key)) {
                stroke = updateRGB(value);
            } else if ("fill-opacity".equals(key)) {
                fillOpacity = Double.parseDouble(value);
            } else if ("stroke-width".equals(key)) {
                if (value.endsWith("px")) {
                    value = value.substring(0, value.length()-2);
                }
                strokeWidth = Double.parseDouble(value);
            } else if ("stroke-opacity".equals(key)) {
                strokeOpacity = Double.parseDouble(value);
            } else {
                logger.debug("unparsed style element "+key);
            }
        }

        StringBuilder strokeDst=null;
        StringBuilder fillDst=null;

        if (stroke != null) {
            strokeDst = new StringBuilder();
            strokeDst.append(paintObjectName +
                            ".setARGB("+ argbParameters(stroke, strokeOpacity) +");\n");
            strokeDst.append(paintObjectName+".setStyle(Paint.Style.STROKE);\n");
        }
        if (fill != null) {
            fillDst  = new StringBuilder();
            fillDst.append(paintObjectName +
                ".setARGB("+ argbParameters(fill, fillOpacity) +");\n");
            fillDst.append(paintObjectName+".setStyle(Paint.Style.FILL);\n");
        }

        if (null != strokeWidth && null != strokeDst) {
            strokeDst.append(paintObjectName+".setStrokeWidth("+strokeWidth+"f * extractScaleFactor(m0) );\n");
        }


        return new DrawStyleParts(strokeDst, fillDst);
    }

    public static String argbParameters(Color fill, Double fillOpacity)
    {
        int alpha = (int) (fillOpacity*255.9);
        return alpha+","+fill.getRed()+", "+fill.getGreen()+", "+fill.getBlue();
    }

    public static Color updateRGB(String stringValue)
    {
        if ("none".equals(stringValue)) {
            return null;
        } else {
            return Color.decode(stringValue);

        }
    }
}
