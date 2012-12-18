package com.purplefrog.svgToAndroid;

import org.apache.log4j.*;
import org.jdom.*;

import java.awt.geom.*;
import java.io.*;
import java.text.*;
import java.util.List;
import java.util.regex.*;

/**
 * internal representation of an SVG &lt;path&gt; element.
 * <p>
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 12/10/12
 * Time: 12:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class SVGPath
    extends SVGBase
{
    private static final Logger logger = Logger.getLogger(SVGPath.class);


    protected AffineTransform xform;
    protected String d;
    protected final StringBuilder drawingCommands;
    private DrawStyleParts paintAdjustments;

    public SVGPath(Element element)
        throws ParseException
    {
        super(element.getAttributeValue("id"));

        xform = parseTransform(element.getAttributeValue("transform"));

        paintAdjustments = parseStyle(element, "paint");

        d = element.getAttributeValue("d");

        drawingCommands = new StringBuilder();
        emitForPath(d, drawingCommands, "rval");
    }

    @Override
    public void writeJavaTo(Writer w)
        throws IOException
    {
        w.write(myPathFunction());
        w.write(myDrawFunction());
    }

    public String myPathFunction()
    {
        StringBuilder rval = new StringBuilder();
        rval.append(clichePathFunctionDeclaration(nameify("pathOnly_"+id)) + "{\n");

        rval.append("Path rval = new Path();\n\n");

        rval.append(drawingCommands);

        rval.append(convertToMatrixString(xform, "m"));
        rval.append("rval.transform(m);\n");

        rval.append("\nreturn rval;\n");
        rval.append("}\n\n");

        return rval.toString();
    }

    public String myDrawFunction()
    {
        StringBuilder rval = new StringBuilder();
        rval.append(clicheFunctionDeclaration() + "{\n");

        rval.append("Path rval = pathOnly_" +nameify(id)+ "();\n");

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

    public static void emitForPath(String path_d, StringBuilder dst, String varName)
        throws ParseException
    {
        Point2D pathStart = new Point2D.Double(0,0);
        Point2D turtle =new Point2D.Double(0,0);
        dst.append(varName+".moveTo(0,0);\n");
        int[] cursor={0};
        while (cursor[0] < path_d.length()) {
            char ch = path_d.charAt(cursor[0]);

            if (Character.isWhitespace(ch)) {
                cursor[0]++;
            } else if (ch == 'm') {
                cursor[0]++;
                Double x = nextNumber(path_d, cursor);
                double y = nextNumber(path_d, cursor);
                dst.append(varName + ".rMoveTo(" + x + "f, " + y + "f);\n");

                turtleDelta(pathStart, x, y);
                turtle.setLocation(pathStart);

                while (true) {
                    x = nextNumber_(path_d, cursor);
                    if (null==x)
                        break;

                    y = nextNumber(path_d, cursor);
                    dst.append(varName + ".rLineTo(" + x + "f, " + y + "f);\n");
                    turtleDelta(turtle, x, y);
                }

            } else if (ch == 'M') {
                cursor[0]++;
                Double x = nextNumber(path_d, cursor);
                double y = nextNumber(path_d, cursor);
                dst.append(varName + ".moveTo(" + x + "f, " + y + "f);\n");

                pathStart.setLocation(x, y);
                turtle.setLocation(pathStart);

                while (true) {
                    x = nextNumber_(path_d, cursor);
                    if (null==x)
                        break;

                    y = nextNumber(path_d, cursor);
                    dst.append(varName + ".lineTo(" + x + "f, " + y + "f);\n");

                    turtle.setLocation(x,y);
                }

            } else if (ch == 'l') {
                cursor[0]++;
                Double x = nextNumber(path_d, cursor);
                double y = nextNumber(path_d, cursor);
                dst.append(varName + ".rLineTo(" + x + "f, " + y + "f);\n");

                turtleDelta(turtle, x, y);

                while (true) {
                    x = nextNumber_(path_d, cursor);
                    if (null==x)
                        break;

                    y = nextNumber(path_d, cursor);
                    dst.append(varName + ".rLineTo(" + x + "f, " + y + "f);\n");

                    turtleDelta(turtle, x, y);
                }

            } else if (ch == 'L') {
                cursor[0]++;
                double x = nextNumber(path_d, cursor);
                double y = nextNumber(path_d, cursor);
                dst.append(varName + ".lineTo(" + x + "f, " + y + "f);\n");
                turtle.setLocation(x,y);

            } else if (ch == 'c') {
                cursor[0]++;
                Double x1 = nextNumber(path_d, cursor);
                while (true) {
                    double y1 = nextNumber(path_d, cursor);
                    double x2 = nextNumber(path_d, cursor);
                    double y2 = nextNumber(path_d, cursor);
                    double x3 = nextNumber(path_d, cursor);
                    double y3 = nextNumber(path_d, cursor);
                    dst.append(varName + ".rCubicTo(" + x1 + "f, " + y1 + "f, "
                        + x2 + "f, " + y2 + "f, "
                        + x3 + "f, " + y3 + "f);\n");

                    turtleDelta(turtle, x3, y3);

                    x1 = nextNumber_(path_d, cursor);
                    if (null==x1)
                        break;
                }
            } else if (ch == 'C') {
                cursor[0]++;
                Double x1 = nextNumber(path_d, cursor);
                while (true) {
                    double y1 = nextNumber(path_d, cursor);
                    double x2 = nextNumber(path_d, cursor);
                    double y2 = nextNumber(path_d, cursor);
                    double x3 = nextNumber(path_d, cursor);
                    double y3 = nextNumber(path_d, cursor);
                    dst.append(varName + ".cubicTo(" + x1 + "f, " + y1 + "f, "
                        + x2 + "f, " + y2 + "f, "
                        + x3 + "f, " + y3 + "f);\n");

                    turtle.setLocation(x3,y3);

                    x1 = nextNumber_(path_d, cursor);
                    if (null==x1)
                        break;
                }

            } else if (ch == 'a') {
                cursor[0]++;
                Double rx = nextNumber(path_d, cursor);
                while (true) {
                    double ry = nextNumber(path_d, cursor);
                    double phi = nextNumber(path_d, cursor);
                    double largeArcFlag = nextNumber(path_d, cursor);
                    double sweepFlag = nextNumber(path_d, cursor);
                    double x = nextNumber(path_d, cursor);
                    double y = nextNumber(path_d, cursor);

                    double x2 = turtle.getX() + x;
                    double y2 = turtle.getY() + y;
                    dst.append("// "+turtle.getX()+" , "+turtle.getY()+"\n");
                    dst.append(emitCodeForArc(varName, turtle.getX(), turtle.getY(), rx, ry, phi, largeArcFlag, sweepFlag, x2, y2));
                    turtle.setLocation(x2, y2);

                    rx = nextNumber_(path_d, cursor);
                    if (null == rx)
                        break;
                }

            } else if (ch == 'A') {
                cursor[0]++;
                Double rx = nextNumber(path_d, cursor);
                while (true) {
                    double ry = nextNumber(path_d, cursor);
                    double phi = nextNumber(path_d, cursor);
                    double largeArcFlag = nextNumber(path_d, cursor);
                    double sweepFlag = nextNumber(path_d, cursor);
                    double x = nextNumber(path_d, cursor);
                    double y = nextNumber(path_d, cursor);

                    dst.append(emitCodeForArc(varName, turtle.getX(), turtle.getY(), rx, ry, phi, largeArcFlag, sweepFlag, x, y));

                    turtle.setLocation(x,y);

                    rx = nextNumber_(path_d, cursor);
                    if (null==rx)
                        break;
                }

            } else if ('z' == ch) {
                cursor[0]++;
                dst.append(varName + ".close();\n");
                dst.append(varName+".moveTo("+pathStart.getX()+"f, "+pathStart.getY() + "f);" +
                    " // work-around for android bug\n");
            } else {
                throw new ParseException("unrecognized SVG <path> element '"+ch+"'", cursor[0]);
            }
        }
    }

    public static void turtleDelta(Point2D turtle, Double x, double y)
    {
        turtle.setLocation(turtle.getX()+x, turtle.getY()+y);
    }

    public static String emitCodeForArc(String varName, double x1, double y1, double rx, double ry, double phi, double largeArcFlag, double sweepFlag, double x2, double y2)
    {
        StringBuilder dst = new StringBuilder();
        SVGArc a1 = new SVGArc(x1, y1, rx, ry, phi, largeArcFlag != 0, sweepFlag != 0, x2, y2);

        AndroidPathArc a2 = convertSVGArcToAndroid(a1);

        dst.append("{\n");

        if (a2.xAxisRotation!=0) {
            dst.append("Matrix mr1 = new Matrix();\n"
                +"mr1.setRotate("+(-a2.xAxisRotation)+"f, "+a2.cx+"f, "+a2.cy+"f);\n");
            dst.append(varName+".transform(mr1);\n");
        }
        dst.append(varName+".arcTo(new RectF(" +
            a2.oval.getX()+"f, "+a2.oval.getY()+"f, "+a2.oval.getMaxX()+"f, "+a2.oval.getMaxY()
            +"f), "+a2.startAngle+"f, "+a2.sweepAngle+"f);\n");
        if (a2.xAxisRotation!=0) {
            dst.append("mr1.setRotate("+(a2.xAxisRotation)+"f, "+a2.cx+"f, "+a2.cy+"f);\n" );
            dst.append(varName+".transform(mr1);\n");
        }
        dst.append("}\n");
        return dst.toString();
    }

    public static double nextNumber(CharSequence buffer, int[] cursor)
        throws ParseException
    {
        Pattern p = Pattern.compile("^[\\s,]*("+DECIMAL_PATTERN+")");
        CharSequence b2 = buffer.subSequence(cursor[0], buffer.length());
        Matcher m = p.matcher(b2);

        if (m.find()) {
            cursor[0] += m.end();
            return Double.parseDouble(m.group(1));
        } else {
            throw new ParseException("couldn't find number after position "+cursor[0], cursor[0]);
        }
    }

    public static Double nextNumber_(CharSequence buffer, int[] cursor)
    {
        Pattern p = Pattern.compile("^[\\s,]*("+DECIMAL_PATTERN+")");
        CharSequence b2 = buffer.subSequence(cursor[0], buffer.length());
        Matcher m = p.matcher(b2);

        if (m.find()) {
            cursor[0] += m.end();
            return Double.parseDouble(m.group(1));
        } else {
            return null;
        }
    }

    @Override
    public List<SVGPart> getChildren()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
