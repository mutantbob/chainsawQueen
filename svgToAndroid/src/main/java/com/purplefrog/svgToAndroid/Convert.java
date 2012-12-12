package com.purplefrog.svgToAndroid;

import org.apache.log4j.*;
import org.jdom.*;
import org.jdom.input.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 12/10/12
 * Time: 11:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class Convert
{
    public static void main(String[] argv)
        throws JDOMException, IOException, ParseException
    {
        BasicConfigurator.configure();


        String ifname = argv[0];
        String ofname = argv[1];

//        ifname = "svgs/test.svg";
//        ofname = "chainsawQueen/gen/com/purplefrog/chainsawQueen/Picture2.java";

        SAXBuilder builder =new SAXBuilder();

        Document doc = builder.build(new File(ifname));


        Element root = doc.getRootElement();

        if (root.getName().equals("svg")) {
            double width = Double.parseDouble(root.getAttributeValue("width"));
            double height = Double.parseDouble(root.getAttributeValue("height"));


            List<Element> l = root.getChildren();

            SVG svg = new SVG(l);

            FileWriter w = new FileWriter(ofname);
            String className = classNameFor(ofname);
            svg.writeClassDeclaration(w, "com.purplefrog.chainsawQueen", className);
            w.flush();
            w.close();
        } else {
            throw new IllegalArgumentException("root element of SVG file should be <svg>");
        }
    }

    private static String classNameFor(String ofname)
    {
        String basename;
        {
            int idx = ofname.lastIndexOf(File.separator);
            if (idx<0)
                basename = ofname;
            else
                basename = ofname.substring(idx+ File.separator.length());
        }
        if (basename.toLowerCase().endsWith(".java")) {
            return basename.substring(0, basename.length() -5);
        } else {
            return basename;
        }
    }
}
