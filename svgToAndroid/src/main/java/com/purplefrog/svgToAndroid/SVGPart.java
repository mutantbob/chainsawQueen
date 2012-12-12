package com.purplefrog.svgToAndroid;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 12/10/12
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
public interface SVGPart
{
    void writeJavaTo(Writer w)
        throws IOException;

    String myFunctionName();

    String getId();

    List<SVGPart> getChildren();

    void setId(String newId);
}
