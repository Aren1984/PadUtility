package com.arenchien.padbackup;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
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

public class MainActivity extends Activity implements ResourceFinder {
    public final Globals m_kLuaGlobals;

    public MainActivity( ) {
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
    ListView m_kListView;
    Button m_kNewBattleButton;
    Button m_kSaveButton;
    Button m_kUpdateButton;
    Button m_kLoadButton;
    Button m_kDeleteButton;
    Button m_kClearButton;
    Button m_kTestButton;
    CheckBox m_kShowBarCheckBox;

    String m_strJapan = "/sdcard/.001.pad";
    String m_strTw = "/sdcard/.002.padtw";
    String m_strSourceFolder = "/files";
    String m_strTargetFolder = "/backup";
    String m_strScriptFolder = "/scripts";
    String m_strTempFolder = "/temp";
    String m_strAccountFolder = "/account";
    String m_strPackageNameJp = "jp.gungho.pad";
    String m_strPackageNameTw = "jp.gungho.padHT";
    String m_strPackageNameThis = "com.example.arenchien.padbackup";
    int m_nSaveButtonIndex;
    int m_nLoadButtonIndex;
    int m_nCloseButtonIndex;
    int m_nTestButtonIndex = -1;
    int m_nActionIndex = -1;
    FloatingWindow m_kFloatingWindow = null;
    RootUtility m_kRootUtility = null;
    CGame m_kGame = new CGame();
    CanvasImageView m_kCanvas;
    FloatingWindow.CImageLayer m_kCanvasImageLayer;

    private boolean IsJapan( ) {
        //RadioButton kRadioButton = ( RadioButton )findViewById( R.id.JapanRadioButton );
        return false;//kRadioButton.isChecked();
    }

    private String GetSourcePath( ) {
        if ( IsJapan() ) {
            return m_strJapan + m_strSourceFolder;
        } else {
            return m_strTw + m_strSourceFolder;
        }
    }

    private String GetTargetPath( ) {
        if ( IsJapan() ) {
            return m_strJapan + m_strTargetFolder;
        } else {
            return m_strTw + m_strTargetFolder;
        }
    }

    private String GetScriptPath( ) {
        if ( IsJapan() ) {
            return m_strJapan + m_strScriptFolder;
        } else {
            return m_strTw + m_strScriptFolder;
        }
    }

    private String GetTempPath( ) {
        if ( IsJapan() ) {
            return m_strJapan + m_strTempFolder;
        } else {
            return m_strTw + m_strTempFolder;
        }
    }

    private String GetPackageName( ) {
        if ( IsJapan() ) {
            return m_strPackageNameJp;
        } else {
            return m_strPackageNameTw;
        }
    }

    private void SetWifiActive( boolean bActive ) {
        WifiManager wifiManager = ( WifiManager ) this.getSystemService( Context.WIFI_SERVICE );
        wifiManager.setWifiEnabled( bActive );
    }

    private boolean GetWifiActive( ) {
        WifiManager wifiManager = ( WifiManager ) this.getSystemService( Context.WIFI_SERVICE );
        return wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
    }

    private static void CopyFile( File kSource, File kTarget ) {
        InputStream kInput = null;
        OutputStream kOutput = null;
        try {
            kInput = new FileInputStream( kSource );
            kOutput = new FileOutputStream( kTarget );
            byte[] aBuffer = new byte[ 1024 ];
            int nBytesRead;
            while ( ( nBytesRead = kInput.read( aBuffer ) ) > 0 ) {
                kOutput.write( aBuffer, 0, nBytesRead );
            }
            kInput.close();
            kOutput.close();
        } catch ( IOException ex ) {

        }
    }


    private void DeleteFolder( File kFolder ) {
        File[] kSubFiles = kFolder.listFiles();
        for ( int i = 0; kSubFiles != null && i < kSubFiles.length; ++i ) {
            if ( kSubFiles[ i ].isDirectory() ) {
                DeleteFolder( kSubFiles[ i ] );
            } else {
                kSubFiles[ i ].delete();
            }
        }
        kFolder.delete();
    }

    private void DeleteFilesInFolder( File kFolder ) {
        File[] kSubFiles = kFolder.listFiles();
        for ( int i = 0; kSubFiles != null && i < kSubFiles.length; ++i ) {
            if ( kSubFiles[ i ].isFile() ) {
                kSubFiles[ i ].delete();
            }
        }
    }

    private void RestartPad( ) {
        ActivityManager am = ( ActivityManager ) this.getSystemService( ACTIVITY_SERVICE );
        if ( am != null ) {
            try {
                am.killBackgroundProcesses( GetPackageName() );
            } catch ( Exception e ) {
                String str = e.getMessage();
            }
        }
    }

    protected void LaunchPad( ) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage( GetPackageName() );
        //launchIntent.setFlags( Intent.FLAG_ACTIVITY_NO_ANIMATION );
        startActivity( launchIntent );
    }

    protected void LaunchThis( ) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage( m_strPackageNameThis );
        launchIntent.setFlags( /*Intent.FLAG_ACTIVITY_NO_ANIMATION | */Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
        startActivity( launchIntent );
    }

    protected void ClearRecord( ) {
        File kTargetFolder = new File( GetTargetPath() );
        File[] kSubFiles = kTargetFolder.listFiles();
        for ( int i = 0; kSubFiles != null && i < kSubFiles.length; ++i ) {
            if ( kSubFiles[ i ].isDirectory() ) {
                DeleteFolder( kSubFiles[ i ] );
            } else {
                kSubFiles[ i ].delete();
            }
        }
    }

    protected void ClearAccount( ) {
        DeleteFilesInFolder( new File( GetSourcePath() ) );
    }

    protected void DeleteRecord( String strName ) {
        String strTargetFolder = GetTargetPath() + "/" + strName;
        File kTargetFolder = new File( strTargetFolder );
        DeleteFolder( kTargetFolder );
    }

    protected void RecordCurrentStage( String strName ) {
        String strTargetFolder = GetTargetPath() + "/" + strName;
        File kTargetFolder = new File( strTargetFolder );
        kTargetFolder.mkdir();
        File kSourceFolder = new File( GetSourcePath() );
        File[] kSubFiles = kSourceFolder.listFiles();
        for ( int i = 0; kSubFiles != null && i < kSubFiles.length; ++i ) {
            if ( kSubFiles[ i ].isFile() ) {
                File kTarget = new File( strTargetFolder + "/" + kSubFiles[ i ].getName() );
                CopyFile( kSubFiles[ i ], kTarget );
            }
        }
    }

    protected void RestoreCurrentStage( String strName ) {
        String strTargetFolder = GetTargetPath() + "/" + strName;
        File kTargetFolder = new File( strTargetFolder );
        File kSourceFolder = new File( GetSourcePath() );
        File[] kSubFiles = kTargetFolder.listFiles();
        for ( int i = 0; kSubFiles != null && i < kSubFiles.length; ++i ) {
            if ( kSubFiles[ i ].isFile() ) {
                File kTarget = new File( kSourceFolder + "/" + kSubFiles[ i ].getName() );
                CopyFile( kSubFiles[ i ], kTarget );
            }
        }
    }

    protected void Restore( String strRecordName ) {
        //SetWifiActive(false);
        RestartPad();
        RestoreCurrentStage( strRecordName );
        LaunchPad();
    }

    protected void Restore( View v, int nIndex, boolean bQuestion ) {
        final StableArrayAdapter kAdapter = ( StableArrayAdapter ) m_kListView.getAdapter();
        final String strName = kAdapter.GetKey( nIndex );

        if ( bQuestion ) {
            DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int which ) {
                    Restore( strName );
                }
            };
            Question( v, "是否要還原" + strName + "?", OkClick );
        } else {
            Restore( strName );
        }
    }


    protected void Question( View v, String strQuestion, DialogInterface.OnClickListener kOkAction ) {
        //final String strName = m_kListView.getItemAtPosition( nIndex ).toString();
        AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder( v.getContext() );
        MyAlertDialog.setTitle( "Warning" );
        MyAlertDialog.setMessage( strQuestion );
        DialogInterface.OnClickListener CancelClick = new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface kDialog, int nWhich ) {
            }
        };
        MyAlertDialog.setPositiveButton( "OK", kOkAction );
        MyAlertDialog.setNeutralButton( "Cancel", CancelClick );
        MyAlertDialog.show();
    }

    protected void RefreshList( int nSelected ) {
        File kTargetFolder = new File( GetTargetPath() );
        File[] kSubFiles = kTargetFolder.listFiles();
        if ( kSubFiles == null ) {
            return;
        }
        final ArrayList< String > list = new ArrayList< String >();
        final ArrayList< String > kKeyList = new ArrayList< String >();
        for ( int i = kSubFiles.length - 1; i >= 0; --i ) {
            if ( kSubFiles[ i ].isDirectory() ) {
                Date kLastModDate = new Date( kSubFiles[ i ].lastModified() );
                SimpleDateFormat formatter = new SimpleDateFormat( "MMM dd HH:mm:ss" );
                list.add( "[" + kSubFiles[ i ].getName() + "] - " + formatter.format( kLastModDate ) );
                kKeyList.add( kSubFiles[ i ].getName() );
            }
        }
        StableArrayAdapter adapter = new StableArrayAdapter( this, android.R.layout.simple_list_item_1, list, kKeyList );
        m_kListView.setAdapter( adapter );
        m_kListView.setSelection( 0 );

        adapter.setSelectItem( nSelected );
    }

    protected void RefreshButtonState( ) {
        m_kNewBattleButton.setEnabled( true );
        m_kSaveButton.setEnabled( true );

        StableArrayAdapter kAdapter = ( ( StableArrayAdapter ) m_kListView.getAdapter() );
        if ( kAdapter == null ) {
            return;
        }
        int nSelectIndex = kAdapter.getSelectItem();
        if ( nSelectIndex == -1 ) {
            m_kUpdateButton.setEnabled( false );
            m_kDeleteButton.setEnabled( false );
            m_kLoadButton.setEnabled( false );
        } else if ( nSelectIndex == kAdapter.GetItemCount() - 1 ) {
            m_kUpdateButton.setEnabled( false );
            m_kDeleteButton.setEnabled( false );
            m_kLoadButton.setEnabled( true );
        } else {
            m_kUpdateButton.setEnabled( true );
            m_kDeleteButton.setEnabled( true );
            m_kLoadButton.setEnabled( true );
        }
    }

    protected void ListFiles( String strPath ) {
        File kRootFiles = new File( strPath );
        File[] kFiles = kRootFiles.listFiles();
        final ArrayList< String > list = new ArrayList< String >();
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

    private int ReadCounter( ) {
        try {
            File kFile = new File( GetTargetPath() + "/counter.log" );
            InputStream kInput = new FileInputStream( kFile );

            byte[] kBuffer = new byte[ 4 ];
            kInput.read( kBuffer );
            ByteBuffer wrapped = ByteBuffer.wrap( kBuffer );
            int nCounter = wrapped.getInt();
            kInput.close();
            return nCounter;
        } catch ( IOException ex ) {
            return 0;
        }
    }

    private void WriteCounter( int nCounter ) {
        try {
            File kFile = new File( GetTargetPath() + "/counter.log" );
            OutputStream kOutput = new FileOutputStream( kFile );
            byte[] kBuffer = ByteBuffer.allocate( 4 ).putInt( nCounter ).array();
            kOutput.write( kBuffer );
            kOutput.close();
        } catch ( IOException ex ) {
        }
    }

    public void Save( ) {
        //SetWifiActive(false);
        int nCounter = ReadCounter();
        ++nCounter;
        WriteCounter( nCounter );
        String strName = String.valueOf( nCounter );
        RecordCurrentStage( strName );
        RefreshList( 0 );
        RefreshButtonState();
        LaunchPad();
    }

    private void Load( View v, boolean bQuestion ) {
        int nSelectIndex = ( ( StableArrayAdapter ) m_kListView.getAdapter() ).getSelectItem();
        if ( nSelectIndex == -1 ) {
            return;
        }
        Restore( v, nSelectIndex, bQuestion );
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        m_kListView = ( ListView ) findViewById( R.id.ListViewRecord );
        m_kNewBattleButton = ( Button ) findViewById( R.id.ButtonNewBattle );
        m_kSaveButton = ( Button ) findViewById( R.id.ButtonSave );
        m_kUpdateButton = ( Button ) findViewById( R.id.ButtonUpdate );
        m_kLoadButton = ( Button ) findViewById( R.id.ButtonLoad );
        m_kDeleteButton = ( Button ) findViewById( R.id.ButtonDelete );
        m_kClearButton = ( Button ) findViewById( R.id.ButtonClear );
        m_kTestButton = ( Button ) findViewById( R.id.ButtonTest );
        m_kShowBarCheckBox = ( CheckBox ) findViewById( R.id.CheckBoxShowBar );
        m_kShowBarCheckBox.setChecked( true );

        m_kShowBarCheckBox.setOnClickListener( new CheckBox.OnClickListener() {
            @Override
            public void onClick( View v ) {
                m_kFloatingWindow.ToggleMainVisible();
            }
        } );

        m_kNewBattleButton.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick( View v ) {
                DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int which ) {
                        //SetWifiActive(false);
                        ClearRecord();
                        RecordCurrentStage( "Init" );
                        RefreshList( 0 );
                        RefreshButtonState();
                        LaunchPad();
                    }
                };
                Question( v, "是否要重新開始?", OkClick );
            }
        } );

        m_kSaveButton.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Save();
            }
        } );

        m_kUpdateButton.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick( View v ) {
                final StableArrayAdapter kAdapter = ( StableArrayAdapter ) m_kListView.getAdapter();
                final int nSelectIndex = kAdapter.getSelectItem();
                if ( nSelectIndex == -1 ) {
                    return;
                }

                DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int which ) {
                        //SetWifiActive(false);
                        String strName = kAdapter.GetKey( nSelectIndex );
                        RecordCurrentStage( strName );
                        RefreshList( nSelectIndex );
                        RefreshButtonState();
                        LaunchPad();
                    }
                };
                Question( v, "是否要覆蓋?", OkClick );
            }
        } );


        m_kLoadButton.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Load( v, true );
            }
        } );


        m_kDeleteButton.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick( View v ) {
                final StableArrayAdapter kAdapter = ( StableArrayAdapter ) m_kListView.getAdapter();
                final int nSelectIndex = kAdapter.getSelectItem();
                if ( nSelectIndex < 0 || nSelectIndex == kAdapter.getCount() - 1 ) {
                    return;
                }

                DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int which ) {
                        String strName = kAdapter.GetKey( nSelectIndex );
                        DeleteRecord( strName );
                        RefreshList( 0 );
                        RefreshButtonState();
                    }
                };
                Question( v, "是否要刪除?", OkClick );
            }
        } );

        m_kClearButton.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick( View v ) {
                DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int which ) {
                        RestartPad();
                        ClearAccount();
                        LaunchPad();
                    }
                };
                Question( v, "是否要清除帳號?", OkClick );
            }
        } );

        m_kTestButton.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if ( m_nTestButtonIndex == -1 ) {
                    m_nTestButtonIndex = m_kFloatingWindow.AddButton( R.drawable.test );
                } else {
                    m_kFloatingWindow.RemoveButton( m_nTestButtonIndex );
                    m_nTestButtonIndex = -1;
                }
            }
        } );


        m_kListView.setOnItemClickListener( new ListView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView parent, View v, int id, long arg3 ) {
                ( ( StableArrayAdapter ) m_kListView.getAdapter() ).setSelectItem( id );
                ( ( StableArrayAdapter ) m_kListView.getAdapter() ).notifyDataSetInvalidated();
                RefreshButtonState();
            }
        } );

        RefreshList( 0 );
        RefreshButtonState();

        Intent kIntent = new Intent( MainActivity.this, FloatingWindow.class );
        startService( kIntent );
        bindService( kIntent, m_kServiceConnection, Context.BIND_AUTO_CREATE );

        try {
            m_kRootUtility = new RootUtility();
        }
        catch ( IOException e ) {
            m_kRootUtility = null;
        }
    }

    @Override
    protected void onResume( ) {
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

    private ServiceConnection m_kServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected( ComponentName name ) {
        }

        @Override
        public void onServiceConnected( ComponentName name, IBinder service ) {
            if ( m_kFloatingWindow != null ) {
                return;
            }
            m_kFloatingWindow = ( ( FloatingWindow.LocalBinder ) service ).getService();

            m_kCanvas = new CanvasImageView( m_kFloatingWindow );
            m_kCanvasImageLayer = m_kFloatingWindow.new CImageLayer( m_kCanvas, 1080, 1920, 0, 0, false );

            m_nSaveButtonIndex = m_kFloatingWindow.AddButton( R.drawable.save );
            m_nLoadButtonIndex = m_kFloatingWindow.AddButton( R.drawable.load );
            m_nCloseButtonIndex = m_kFloatingWindow.AddButton( R.drawable.close );
            m_kFloatingWindow.SetButtonClickedListener( new FloatingWindow.OnButtonClickedListener() {
                @Override
                public void OnButtonClicked( View v, int nButtonIndex ) {
                    if ( nButtonIndex == m_nCloseButtonIndex ) {
                        m_kFloatingWindow.ToggleMainVisible();
                        m_kShowBarCheckBox.setChecked( false );
                    }
                    else if ( nButtonIndex == m_nTestButtonIndex ) {
                        //String strFile = GetTempPath() + "/temp.png";
                        //TakeScreenshot( strFile );
                        Screen kScreen = m_kRootUtility.GetScreen();
                        CGame.CBoard kBoard = m_kGame.Analysis( kScreen );
                        kBoard.DrawGrid( m_kCanvas);
                        m_kCanvasImageLayer.Show();
                    } else {
                        LaunchThis();
                    }
                    m_nActionIndex = nButtonIndex;
                }
            } );
        }
    };

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate( R.menu.menu_main, menu );
        return false;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if ( id == R.id.action_settings ) {
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    private class StableArrayAdapter extends ArrayAdapter< String > {

        HashMap< String, Integer > mIdMap = new HashMap< String, Integer >();
        HashMap< Integer, String > mKeyMap = new HashMap< Integer, String >();
        private int nSelectItem = -1;

        public StableArrayAdapter( Context context, int textViewResourceId, List< String > objects, List< String > kKeyList ) {
            super( context, textViewResourceId, objects );
            for ( int i = 0; i < objects.size(); ++i ) {
                mIdMap.put( objects.get( i ), i );
            }
            for ( int i = 0; i < objects.size(); ++i ) {
                mKeyMap.put( i, kKeyList.get( i ) );
            }
        }

        public void setSelectItem( int nSelectItem ) {
            this.nSelectItem = nSelectItem;
        }

        public int getSelectItem( ) {
            return this.nSelectItem;
        }

        public String GetKey( int nItemIndex ) {
            return mKeyMap.get( nItemIndex );
        }

        public int GetItemCount( ) {
            return mIdMap.size();
        }

        @Override
        public long getItemId( int position ) {
            String item = getItem( position );
            return mIdMap.get( item );
        }

        @Override
        public boolean hasStableIds( ) {
            return true;
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            final View renderer = super.getView( position, convertView, parent );
            if ( position == nSelectItem ) {
                renderer.setBackgroundResource( android.R.color.darker_gray );
            } else {
                renderer.setBackgroundResource( android.R.color.white );
            }
            return renderer;
        }

    }
}