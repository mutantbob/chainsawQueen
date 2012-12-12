package com.purplefrog.svgToAndroid;

import org.apache.log4j.*;
import org.jdom.*;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 12/10/12
 * Time: 11:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class SVGImage
    extends SVGBase
{
    private static final Logger logger = Logger.getLogger(SVGImage.class);

    public SVGImage(Element element)
    {
        super(element.getAttributeValue("id"));
        logger.warn("image, NYI", new Exception("stack trace"));
    }

    public void writeJavaTo(Writer w)
        throws IOException
    {
        // unsupported
    }

    public List<SVGPart> getChildren()
    {
        return null;
    }

}
