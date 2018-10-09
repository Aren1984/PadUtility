package com.arenchien.padutility;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.ResourceFinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static java.lang.Thread.sleep;

public class MainActivity extends Activity implements ResourceFinder {
    public final Globals m_kLuaGlobals;

    public MainActivity() {
        m_kLuaGlobals = org.luaj.vm2.lib.jse.JsePlatform.standardGlobals();
        m_kLuaGlobals.finder = this;

        m_kThis = this;
    }

    @Override
    public InputStream findResource( String strFileName ) {
        InputStream kStream;
        try {
            File kFile = new File( GetScriptPath() + "/" + strFileName );
            kStream = new FileInputStream( kFile );
        } catch ( Exception e ) {
            return null;
        }
        return kStream;
    }

    MainActivity m_kThis;




    RootUtility m_kRootUtility = null;
    CGame m_kGame = new CGame();


    private void SetWifiActive( boolean bActive ) {
        WifiManager wifiManager = (WifiManager) this.getSystemService( Context.WIFI_SERVICE );
        wifiManager.setWifiEnabled( bActive );
    }

    private boolean GetWifiActive() {
        WifiManager wifiManager = (WifiManager) this.getSystemService( Context.WIFI_SERVICE );
        return wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
    }












    protected void ListFiles( String strPath ) {
        File kRootFiles = new File( strPath );
        File[] kFiles = kRootFiles.listFiles();
        final ArrayList<String> list = new ArrayList<String>();
        for ( int i = 0; kFiles != null && i < kFiles.length; ++i ) {
            String strPreFix = "\n";
            if ( kFiles[ i ].isDirectory() ) {
                strPreFix += "[D]";
            }
            if ( kFiles[ i ].isFile() ) {
                strPreFix += "[F]";
            }
            if ( kFiles[ i ].isHidden() ) {
                strPreFix += "[H]";
            }
            if ( kFiles[ i ].canRead() ) {
                strPreFix += "[R]";
            }
            if ( kFiles[ i ].canWrite() ) {
                strPreFix += "[W]";
            }
            if ( kFiles[ i ].isAbsolute() ) {
                strPreFix += "[A]";
            }

            list.add( kFiles[ i ].getAbsolutePath() + strPreFix );
        }
    }




    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );


        RefreshList( 0 );
        RefreshButtonState();

        Intent kIntent = new Intent( MainActivity.this, FloatingWindow.class );
        startService( kIntent );
        bindService( kIntent, m_kServiceConnection, Context.BIND_AUTO_CREATE );

        try {
            m_kRootUtility = new RootUtility();
        } catch ( IOException e ) {
            m_kRootUtility = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( m_nActionIndex == -1 ) {
            return;
        }

        if ( m_nActionIndex == m_nSaveButtonIndex ) {
            Save();
        } else if ( m_nActionIndex == m_nLoadButtonIndex ) {
            Load( findViewById( android.R.id.content ), true );
        }

        m_nActionIndex = -1;
    }



    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate( R.menu.menu_main, menu );
        return false;
    }


}