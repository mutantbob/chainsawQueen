package com.purplefrog.chainsawQueen;

import android.app.*;
import android.graphics.*;
import android.os.*;
import android.util.*;
import android.widget.*;

import java.io.*;

/**
 * This activity does nothing but create a snapshot of the wallpaper using {@link com.purplefrog.chainsawQueen.ChainsawQueen.MyEngine#drawFrame_(android.graphics.Canvas)}.
 * The snapshot is saved to {@link #ofname}.
 * Since this activity is entirely useless to anyone but the developer
 * its clauses in AndroidManifest.xml are commented out
 * and it will be inaccessible to normal users.
 */
public class CreateThumbnail
    extends Activity
{
    private static final String LOG_TAG = CreateThumbnail.class.getName();


    public static final String ofname = "/sdcard/thumb.png";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        String msg;
        try {
            makeThumbnail();
            msg = "your thumbnail has been saved to "+ofname;
        } catch (IOException e) {
            msg = "malfunction creating thumbnail";
            Log.w(LOG_TAG, msg, e);
            msg += " : "+e.getMessage();
        }

        TextView tv = (TextView) findViewById(R.id.text);
        tv.setText(msg);
    }

    public static void makeThumbnail()
        throws IOException
    {
        Bitmap b = Bitmap.createBitmap(192,192, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        ChainsawQueen q = new ChainsawQueen();
        ChainsawQueen.MyEngine e = q.new MyEngine();

        e.drawFrame_(c);

        OutputStream stream = new FileOutputStream(ofname);

        b.compress(Bitmap.CompressFormat.PNG, 80, stream);
        stream.close();
    }
}
