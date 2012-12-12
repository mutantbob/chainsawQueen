import com.purplefrog.svgToAndroid.*;
import junit.framework.*;

import java.text.*;

/**
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 12/10/12
 * Time: 1:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class Test1
    extends TestCase
{
    public void test1()
        throws ParseException
    {
        int[] cursor = {0};
        double x = SVGPath.nextNumber(" 0,0", cursor);
        assertEquals(0.0, x);
        assertEquals(2, cursor[0]);
    }
}
