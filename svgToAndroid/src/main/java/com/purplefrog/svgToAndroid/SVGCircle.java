package com.purplefrog.svgToAndroid;

import org.jdom.*;

import java.awt.geom.*;
import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 12/11/12
 * Time: 4:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class SVGCircle
    extends SVGBase
{
    private AffineTransform xform;
    private double cx, cy, r;
    private DrawStyleParts paintAdjustments;

    public SVGCircle(Element element)
    {
        super(element.getAttributeValue("id"));

        paintAdjustments = SVGBase.parseStyle(element, "paint");

        xform=parseTransform(element.getAttributeValue("transform"));

        cx = Double.parseDouble(element.getAttributeValue("cx"));
        cy = Double.parseDouble(element.getAttributeValue("cy"));
        r = Double.parseDouble(element.getAttributeValue("r"));
    }


    @Override
    public void writeJavaTo(Writer w)
        throws IOException
    {
        w.write(myOneFunction());
    }

    public String myOneFunction()
    {
        StringBuilder rval = new StringBuilder();
        rval.append(clicheFunctionDeclaration() + "{\n");

        rval.append("Path rval = new Path();\n");

        rval.append("rval.arcTo(new RectF("+(cx-r)+"f, "+(cy-r)+"f, " +(cx+r)+"f, " + (cy +r)+"f), 0, 180, true);\n");
        rval.append("rval.arcTo(new RectF("+(cx-r)+"f, "+(cy-r)+"f, " +(cx+r)+"f, " + (cy +r)+"f), 180, 180);\n");
        rval.append("rval.close();\n");

        rval.append(convertToMatrixString(xform, "m"));
        rval.append("rval.transform(m);\n");

        rval.append("rval.transform(m0);\n");

        rval.append("Paint paint = new Paint(p0);\n" );

        if (null != paintAdjustments.fillOnly) {
            rval.append(paintAdjustments.fillOnly);
            rval.append("c.drawPath(rval, paint);\n");
        }

        if (null != paintAdjustments.strokeOnly) {
            rval.append(paintAdjustments.strokeOnly);
            rval.append("c.drawPath(rval, paint);\n");
        }

        rval.append("}\n");
        return rval.toString();
    }

    @Override
    public List<SVGPart> getChildren()
    {
        return null;
    }
}
