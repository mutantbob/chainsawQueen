package com.purplefrog.svgToAndroid;

import org.jdom.*;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 12/10/12
 * Time: 4:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class SVGRect
    extends SVGBase
{
    public double height;
    public double width;
    public double x;
    public double y;
    private DrawStyleParts paintAdjustments;

    public SVGRect(Element element)
    {
        super(element.getAttributeValue("id"));

        paintAdjustments = SVGBase.parseStyle(element, "paint");

        width = Double.parseDouble(element.getAttributeValue("width"));
        height = Double.parseDouble(element.getAttributeValue("height"));
        x = Double.parseDouble(element.getAttributeValue("x"));
        y = Double.parseDouble(element.getAttributeValue("y"));
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

        rval.append("rval.moveTo("+x+"f, "+y+"f);\n");
        rval.append("rval.rLineTo(0, "+height+"f);\n");
        rval.append("rval.rLineTo(" +width+"f, 0);\n");
        rval.append("rval.rLineTo(0, "+-height+"f);\n");
        rval.append("rval.close();\n");

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



    private String paint()
    {
        return "Paint paint = new Paint(p0);\n" + paintAdjustments;
    }


    @Override
    public List<SVGPart> getChildren()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
