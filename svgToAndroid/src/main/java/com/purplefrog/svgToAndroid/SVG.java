package com.purplefrog.svgToAndroid;

import org.apache.log4j.*;
import org.jdom.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Our internal representation of the SVG document.
 * Its job is to convert the graphical elements of the SVG into java code for Android.
 *
 * <p></p>
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 12/10/12
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class SVG
{
    private static final Logger logger = Logger.getLogger(SVG.class);
    private List<SVGPart> subparts;

    public SVG(List<Element> l)
        throws ParseException
    {
        subparts = parseSVGParts(l);

        deanonymize(subparts);
    }

    public static ArrayList<SVGPart> parseSVGParts(List<Element> l)
        throws ParseException
    {
        ArrayList<SVGPart> x = new ArrayList<SVGPart>();
        for (Element element : l) {

            SVGPart y = parseSVGPart(element);

            if (null != y)
                x.add(y);
        }

        return x;
    }

    public static void deanonymize(List<SVGPart> x)
    {
        Set<String> known = new HashSet<String>();
        collectKnownIds(x, known);
        deanonymize(x, known);
    }

    private static void collectKnownIds(List<SVGPart> parts, Set<String> known)
    {
        for (SVGPart part : parts) {
            known.add(part.getId());
            List<SVGPart> children = part.getChildren();
            if (null != children)
                deanonymize(children, known);
        }
    }

    private static void deanonymize(List<SVGPart> parts, Set<String> knownIds)
    {
        for (SVGPart part : parts) {
            if (part.getId()==null) {
                part.setId(makeUpId(knownIds));
            }
            List<SVGPart> children = part.getChildren();
            if (null != children)
                deanonymize(children,knownIds);
        }
    }

    public static String makeUpId(Set<String> knownIds)
    {
        int i=1;
        while (true) {
            String candidate = "anonymous"+i;
            if (knownIds.contains(candidate)) {
                i++;
            } else {
                knownIds.add(candidate);
                return candidate;
            }
        }
    }

    public static SVGPart parseSVGPart(Element element)
        throws ParseException
    {
        String ename = element.getName();
        if ("g".equals(ename)) {
            return new SVGGroup(element);
        } else if ("image".equals(ename)) {
//            y= new SVGImage(element);  XXX NYI
            return null;
        } else if ("path".equals(ename)) {
            return new SVGPath(element);
        } else if ("rect".equals(ename)) {
            return new SVGRect(element);
        } else if ("circle".equals(ename)) {
            return new SVGCircle(element);
        } else if ("ellipse".equals(ename)) {
            return new SVGEllipse(element);
        } else {
            if (dontCareAboutElement(ename)) {
                logger.debug("unrecognized SVG element <" + ename + ">");
            } else {
                logger.warn("unrecognized SVG element <"+ename+">");
            }
            return null;
        }
    }

    private static boolean dontCareAboutElement(String ename)
    {
        return "defs".equals(ename)
            || "namedview".equals(ename)
            || "metadata".equals(ename);
    }

    public void writeJavaTo(Writer ostr)
        throws IOException
    {
        for (SVGPart subpart : subparts) {
            subpart.writeJavaTo(ostr);
            ostr.write("\n");
        }
        ostr.write(myOneFunction());
    }

    public void writeClassDeclaration(Writer w, String packageName, String classname)
        throws IOException
    {
        w.write("package "+packageName+";\n\n");
        w.write("import android.graphics.*;\n\n");
        w.write("public class "+classname+"{\n");

        w.write(extraFunctions());

        writeJavaTo(w);
        w.write("}\n");
    }

    private String extraFunctions()
    {
        return "public static float extractScaleFactor(Matrix m0)\n" +
            "{\n" +
            "    float[] mf = new float[9];\n" +
            "    m0.getValues(mf);\n" +
            "    return (float) Math.sqrt(mf[0]*mf[0] + mf[1]*mf[1]);\n"
            +"}\n" +
            "\n";
    }

    private String myOneFunction()
    {
        String indent = "    ";
        StringBuilder rval = new StringBuilder();

        rval.append(SVGBase.clicheFunctionDeclaration(myFunctionName())+ "{\n");

        for (SVGPart subpart : subparts) {
            rval.append(indent+subpart.myFunctionName()+"(c, m0, p0);\n");
        }

        rval.append("}\n");

        return rval.toString();
    }

    private String myFunctionName()
    {
        return "svg_document";
    }
}
