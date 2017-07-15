package com.arenchien.padbackup;

import android.os.Debug;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Aren Chien on 2016/4/27.
 */

public class CUtility {
    int i = 0;
    private DataOutputStream mRootWapperCommander = null;
    private DataInputStream mRootWapperReader = null;

    static public boolean TakeScreenshot( String strFilePath ) {
        try {
            //long t1 = System.currentTimeMillis();

            Process sh = Runtime.getRuntime().exec( "su", null, null );
            OutputStream os = sh.getOutputStream();
            InputStream is = sh.getInputStream();
            os.write( ( "/system/bin/screencap -p " + strFilePath + "\n" ).getBytes( "ASCII" ) );
            os.flush();
            os.write( ( "echo -n 0\n" ).getBytes( "ASCII" ) );
            os.flush();
            is.read();
            //Log.d( "Debug", Long.toString( System.currentTimeMillis() - t1 ) );
            os.write( ( "chmod 777 " + strFilePath + "\n" ).getBytes( "ASCII" ) );
            os.flush();
            os.close();

/*
            Process sh = Runtime.getRuntime().exec("su", null,null);
            OutputStream  os = sh.getOutputStream();
            os.write(("/system/bin/screencap -p | sed 's/\\r$//' > " + strFilePath).getBytes("ASCII"));
            os.flush();
            os.close();
            sh.waitFor();
            Log.d( "Debug4", Long.toString(System.currentTimeMillis() -  t1) );
*/
        } catch ( Exception e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }

}