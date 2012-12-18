package com.purplefrog.svgToAndroid;

import org.apache.log4j.*;
import org.jdom.*;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.List;
import java.util.regex.*;

/**
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 12/10/12
 * Time: 12:07 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class SVGBase
    implements SVGPart
{
    private static final Logger logger = Logger.getLogger(SVGBase.class);

    public static final String DECIMAL_PATTERN = "-?[0-9]+(?:\\.[0-9]*)?";
    protected String id;

    public SVGBase(String id1)
    {
        id = id1;
    }

    public abstract void writeJavaTo(Writer w)
        throws IOException;

    public String myFunctionName()
    {
        return nameify(id);
    }

    public static String nameify(String svgId)
    {
        return svgId.replaceAll("[- ]", "_");
    }

    public String getId()
    {
        return id;
    }

    public abstract List<SVGPart> getChildren();

    public void setId(String newId)
    {
        this.id = newId;
    }

    public String clicheFunctionDeclaration()
    {
        return clicheFunctionDeclaration(myFunctionName());
    }

    public static String clicheFunctionDeclaration(String fname)
    {
        return "public static void "+ fname +"(Canvas c, Matrix m0, Paint p0)\n";
    }

    public static String clichePathFunctionDeclaration(String fname)
    {
        return "public static Path "+ fname +"()\n";
    }

    public static DrawStyleParts parseStyle(Element element, String paintObjectName)
    {
        return DrawStyleParts.parseStyle(element.getAttributeValue("style"), "paint");
    }

    public static void parseStyle(String style, StringBuilder dst, String paintObjectName)
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
                fill = DrawStyleParts.updateRGB(value);
            } else if ("stroke".equals(key)) {
                stroke = DrawStyleParts.updateRGB(value);
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

        if (stroke != null) {
            dst.append(paintObjectName +
                            ".setARGB("+ DrawStyleParts.argbParameters(stroke, strokeOpacity) +");\n");
            dst.append(paintObjectName+".setStyle(Paint.Style.STROKE);\n");
        }
        if (fill != null) {
            dst.append(paintObjectName +
                ".setARGB("+ DrawStyleParts.argbParameters(fill, fillOpacity) +");\n");
            dst.append(paintObjectName+".setStyle(Paint.Style.FILL);\n");
        }

        if (null != strokeWidth) {
            dst.append(paintObjectName+".setStrokeWidth("+strokeWidth+"f * extractScaleFactor(m0) );\n");
        }
    }

    private static Color updateAlpha(Color oldColor, int a)
    {
        if (oldColor ==null) {
            oldColor = new Color(0,0,0,a);
        } else {
            oldColor = new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), a);
        }
        return oldColor;
    }

    public static AffineTransform parseTransform(String transform)
    {
        if (null == transform)
            return new AffineTransform();

        {
            Pattern p = Pattern.compile("translate\\s*\\(\\s*(" + DECIMAL_PATTERN + ")\\s*,\\s*(" + DECIMAL_PATTERN + ")\\s*\\)");
            Matcher m = p.matcher(transform);
            if (m.find()) {
                AffineTransform rval = new AffineTransform();
                rval.translate(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)));

                rval.concatenate( parseTransform(transform.substring(m.end())) );
                return rval;

            }
        }

        {
            Pattern p = Pattern.compile("matrix\\s*\\(" +
                "\\s*(" + DECIMAL_PATTERN + ")\\s*," +
                "\\s*(" + DECIMAL_PATTERN + ")\\s*," +
                "\\s*(" + DECIMAL_PATTERN + ")\\s*," +
                "\\s*(" + DECIMAL_PATTERN + ")\\s*," +
                "\\s*(" + DECIMAL_PATTERN + ")\\s*," +
                "\\s*(" + DECIMAL_PATTERN + ")\\s*" +
                "\\)");
            Matcher m = p.matcher(transform);
            if (m.find()) {
                double a = Double.parseDouble(m.group(1));
                double b = Double.parseDouble(m.group(2));
                double c = Double.parseDouble(m.group(3));
                double d = Double.parseDouble(m.group(4));
                double e = Double.parseDouble(m.group(5));
                double f = Double.parseDouble(m.group(6));
                AffineTransform rval = new AffineTransform(a,b,c,d,e,f);

                rval.concatenate( parseTransform(transform.substring(m.end())) );
                return rval;

            }
        }


        return new AffineTransform();
    }

    public static String convertToMatrixString(AffineTransform xform, String varName)
    {
        StringBuilder rval = new StringBuilder();
        rval.append("Matrix " + varName + " = new Matrix();\n");

        double[] a = new double[6];
        xform.getMatrix(a);
        rval.append(varName + ".setValues(new float[] { " + a[0] + "f, " + a[2] + "f, "+ a[4]+"f, "
            + a[1] + "f, " + a[3] + "f, " + a[5] + "f," +
            " 0, 0, 1});\n");

        return rval.toString();
    }

    public static String nameOrId(Element element)
    {
        Namespace ns = Namespace.getNamespace("http://www.inkscape.org/namespaces/inkscape");
        String rval = element.getAttributeValue("label", ns);
        if (null != rval)
            return rval;
        return element.getAttributeValue("id");
    }

    /**
     * the parameters that define an SVG arc.
     */
    public static class SVGArc
    {
        double cursorX, cursorY;
        double rx, ry;
        double xAxisRotation;
        boolean largeArcFlag;
        boolean sweepFlag;
        double x, y;

        public SVGArc(double cursorX, double cursorY, double rx, double ry,
                      double xAxisRotation,
                      boolean largeArcFlag, boolean sweepFlag,
                      double x, double y)
        {
            this.cursorX = cursorX;
            this.cursorY = cursorY;
            this.rx = rx;
            this.ry = ry;
            this.xAxisRotation = xAxisRotation;
            this.largeArcFlag = largeArcFlag;
            this.sweepFlag = sweepFlag;
            this.x = x;
            this.y = y;
        }

        public double step2CrazyRadical(double x1p, double y1p)
        {
            double d = rx*rx*y1p*y1p + ry*ry*x1p*x1p;
            double n = rx*rx*ry*ry - d;

            return Math.sqrt(n/d);
        }
    }

    public static class AndroidPathArc
    {
        public Rectangle2D oval;
        public float startAngle, sweepAngle;

        public double xAxisRotation;
        public double cx, cy;

        public AndroidPathArc(Rectangle2D oval, double startAngle, double sweepAngle, double xAxisRotation, double cx, double cy)
        {
            this.oval = oval;
            this.startAngle = (float) startAngle;
            this.sweepAngle = (float) sweepAngle;
            this.xAxisRotation = xAxisRotation;
            this.cx = cx;
            this.cy = cy;
        }
    }

    /**
     *http://www.w3.org/TR/SVG/implnote.html#ArcImplementationNotes
     */
    public static AndroidPathArc convertSVGArcToAndroid(SVGArc svg)
    {
        double cosPhi = Math.cos(svg.xAxisRotation*Math.PI/180);
        double sinPhi = Math.sin(svg.xAxisRotation*Math.PI/180);

        double mx = (svg.cursorX - svg.x)/2;
        double my = (svg.cursorY - svg.y)/2;
        double x1p = cosPhi*mx + sinPhi*my;
        double y1p = -sinPhi*mx + cosPhi*my;

        double q;
        {
            double gamma = x1p*x1p/(svg.rx*svg.rx) + y1p*y1p/(svg.ry*svg.ry);
            if (gamma>1) {
                double gammaR = Math.sqrt(gamma);
                svg.rx *= gammaR;
                svg.ry *= gammaR;
                q=0;
            } else {
                q = svg.step2CrazyRadical(x1p, y1p);
            }
        }

        double cxp = q*svg.rx*y1p/svg.ry;
        double cyp = - q*svg.ry*x1p/svg.rx;

        if (svg.largeArcFlag == svg.sweepFlag) {
            cxp = -cxp;
            cyp = -cyp;
        }

        double cx = cosPhi*cxp - sinPhi*cyp + (svg.cursorX + svg.x) /2;
        double cy = sinPhi*cxp + cosPhi*cyp + (svg.cursorY + svg.y) /2;

        double dx2 = (x1p-cxp)/svg.rx;
        double dy2 = (y1p-cyp)/svg.ry;

        double dx3 = (-x1p-cxp)/svg.rx;
        double dy3 = (-y1p-cyp)/svg.ry;

        double theta1 = chooseAngle(1,0,dx2,dy2);
        double deltaTheta = chooseAngle(dx2, dy2, dx3, dy3);

        if (svg.sweepFlag) {
            if (deltaTheta <0)
                deltaTheta += 2*Math.PI;
        } else {
            if (deltaTheta >0) {
                deltaTheta -= 2*Math.PI;
            }
        }

        Rectangle2D oval = new Rectangle2D.Double(cx-svg.rx, cy-svg.ry, 2 * svg.rx, 2*svg.ry);

        return new AndroidPathArc(oval, theta1*180/Math.PI, deltaTheta*180/Math.PI, svg.xAxisRotation, cx, cy);
    }

    /**
     * compute the angle difference from vector u to v
     * @return radians
     */
    public static double chooseAngle(double ux, double uy, double vx, double vy)
    {
        double n = ux * vx + uy * vy;
        double d = magnitude(ux, uy) * magnitude(vx,vy);
        double rval = Math.acos(n / d);
        double cross = ux*vy - uy*vx;
        if (cross <0) {
            return -rval;
        } else {
            return rval;
        }
    }

    /**
     * standard cartesian vector length
     * @return
     */
    public static double magnitude(double dx, double dy)
    {
        return Math.sqrt(dx*dx+dy*dy);
    }
}
