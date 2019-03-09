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
import android.widget.TabHost;

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

public class MainActivity extends Activity {
    public MainActivity() {

    }

    //RootUtility m_kRootUtility = null;
    CGame m_kGame = new CGame();
    CPadUI m_kPad;
    CPokemonGoUI m_kPokemonGo;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        TabHost tabHost = ( TabHost ) findViewById( R.id.tabHost );
        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec( "tab1" );
        spec.setContent( R.id.tabPokemoinGo );
        spec.setIndicator( "PokemoinGo" );
        tabHost.addTab( spec );

        spec = tabHost.newTabSpec( "tab2" );
        spec.setIndicator( "Pad" );
        spec.setContent( R.id.tabPad );
        tabHost.addTab( spec );


        m_kPad = new CPadUI();
        m_kPad.onCreate( this );

        m_kPokemonGo = new CPokemonGoUI();
        m_kPokemonGo.onCreate( this );

        //try {
        //    m_kRootUtility = new RootUtility();
        //}
        //catch ( IOException e ) {
        //    m_kRootUtility = null;
        //}
    }

    @Override
    protected void onResume() {
        super.onResume();
        m_kPad.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate( R.menu.menu_main, menu );
        return false;
    }


}