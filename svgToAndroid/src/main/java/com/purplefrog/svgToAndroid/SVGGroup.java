package com.purplefrog.svgToAndroid;

import org.jdom.*;

import java.awt.geom.*;
import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 12/10/12
 * Time: 11:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class SVGGroup
    extends SVGBase
{

    protected List<SVGPart> subparts;
    private AffineTransform xform;

    public SVGGroup(Element element)
        throws ParseException
    {
        super(nameOrId(element));

        subparts = SVG.parseSVGParts(element.getChildren());

        xform = SVGPath.parseTransform(element.getAttributeValue("transform"));
    }

    private static String nameOrId(Element element)
    {
        Namespace ns = Namespace.getNamespace("http://www.inkscape.org/namespaces/inkscape");
        String rval = element.getAttributeValue("label", ns);
        if (null != rval)
            return rval;
        return element.getAttributeValue("id");
    }


    @Override
    public void writeJavaTo(Writer w)
        throws IOException
    {
        for (SVGPart subpart : subparts) {
            subpart.writeJavaTo(w);
            w.write("\n");
        }

        w.write(myOneFunction());
    }

    private String myOneFunction()
    {
        String indent = "    ";
        StringBuilder rval = new StringBuilder();

        rval.append(clicheFunctionDeclaration() + "{\n");

        {
            rval.append(convertToMatrixString(xform, "ml"));
            rval.append("Matrix m2 = new Matrix();\n");
            rval.append("m2.setConcat(m0,ml);\n");
        }

        for (SVGPart subpart : subparts) {
            rval.append(indent+subpart.myFunctionName()+"(c, m2, p0);\n");
        }

        rval.append("}\n");

        return rval.toString();
    }

    @Override
    public List<SVGPart> getChildren()
    {
        return subparts;
    }

}
