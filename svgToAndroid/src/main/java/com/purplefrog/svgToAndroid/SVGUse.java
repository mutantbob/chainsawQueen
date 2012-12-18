package com.purplefrog.svgToAndroid;

import org.jdom.*;

import java.awt.geom.*;
import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 12/17/12
 * Time: 3:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class SVGUse
    extends SVGBase
{
    private String href;
    private AffineTransform xform;

    public SVGUse(Element element)
    {
        super(nameOrId(element));

        href = element.getAttributeValue("href", Namespace.getNamespace("http://www.w3.org/1999/xlink"));
        if (!href.startsWith("#")) {
            throw new IllegalArgumentException("I don't handle hrefs that do not refer to internal objects ("+href+")");
        }

        xform = parseTransform(element.getAttributeValue("transform"));
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

        rval.append(convertToMatrixString(xform, "m"));
        rval.append("m.postConcat(m0);\n");

        rval.append(hrefFunctionName()+"(c,m, p0);\n");

        rval.append("}\n");
        return rval.toString();
    }

    private CharSequence hrefFunctionName()
    {
        return nameify(href.substring(1));
    }


    @Override
    public List<SVGPart> getChildren()
    {
        return null;
    }
}
