package com.arenchien.padutility;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ArenChien on 2018/10/9.
 */

public class CPadUI {
    Activity m_kActivity;
    CStableListView m_kListView;
    Button m_kNewBattleButton;
    Button m_kSaveButton;
    Button m_kLoadButton;
    Button m_kDeleteButton;
    Button m_kClearButton;
    Button m_kTestButton;
    CheckBox m_kShowBarCheckBox;

    CanvasImageView m_kCanvas;
    FloatingWindow m_kFloatingWindow = null;
    FloatingWindow.CImageLayer m_kCanvasImageLayer;
    int m_nSaveButtonIndex;
    int m_nLoadButtonIndex;
    int m_nCloseButtonIndex;
    int m_nTestButtonIndex = -1;
    int m_nActionIndex = -1;

    CPadData kPadData = new CPadData();

    public void onCreate( Activity kActivity ) {
        m_kActivity = kActivity;
        m_kListView = new CStableListView( m_kActivity, R.id.ListViewRecord );
        m_kNewBattleButton = ( Button ) m_kActivity.findViewById( R.id.ButtonNewBattle );
        m_kSaveButton = ( Button ) m_kActivity.findViewById( R.id.ButtonSave );
        m_kLoadButton = ( Button ) m_kActivity.findViewById( R.id.ButtonLoad );
        m_kDeleteButton = ( Button ) m_kActivity.findViewById( R.id.ButtonDelete );
        m_kClearButton = ( Button ) m_kActivity.findViewById( R.id.ButtonClear );
        m_kTestButton = ( Button ) m_kActivity.findViewById( R.id.ButtonTest );
        m_kShowBarCheckBox = ( CheckBox ) m_kActivity.findViewById( R.id.CheckBoxShowBar );
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
                        kPadData.ClearRecord();
                        kPadData.RecordCurrentStage( "Init" );
                        RefreshList( 0 );
                        RefreshButtonState();
                        LaunchPad();
                    }
                };
                CUiUtility.Question( v, "是否要重新開始?", OkClick );
            }
        } );

        m_kSaveButton.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Save();
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
                final int nSelectIndex = m_kListView.GetSelectedIndex();
                if ( nSelectIndex < 0 || nSelectIndex == m_kListView.GetCount() - 1 ) {
                    return;
                }

                DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int which ) {
                        String strName = m_kListView.GetText( nSelectIndex );
                        kPadData.DeleteRecord( strName );
                        RefreshList( 0 );
                        RefreshButtonState();
                    }
                };
                CUiUtility.Question( v, "是否要刪除?", OkClick );
            }
        } );

        m_kClearButton.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick( View v ) {
                DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int which ) {
                        StopPad();
                        kPadData.ClearAccount();
                        LaunchPad();
                    }
                };
                CUiUtility.Question( v, "是否要清除帳號?", OkClick );
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
                m_kListView.SetSelectedIndex( id, true );
                RefreshButtonState();
            }
        } );

        RefreshList( 0 );
        //RefreshButtonState();

        Intent kIntent = new Intent( m_kActivity, FloatingWindow.class );
        m_kActivity.startService( kIntent );
        m_kActivity.bindService( kIntent, m_kServiceConnection, Context.BIND_AUTO_CREATE );
    }

    protected void LaunchPad( ) {
        CUiUtility.LaunchApp( m_kActivity, kPadData.GetPackageName() );
    }

    private void StopPad( ) {
        CUiUtility.StopApp( m_kActivity, kPadData.GetPackageName() );
    }

    protected void RefreshButtonState( ) {
        m_kNewBattleButton.setEnabled( true );
        m_kSaveButton.setEnabled( true );

        int nSelectIndex = m_kListView.GetSelectedIndex();
        if ( nSelectIndex == -1 ) {
            m_kDeleteButton.setEnabled( false );
            m_kLoadButton.setEnabled( false );
        } else if ( nSelectIndex == m_kListView.GetCount() - 1 ) {
            m_kDeleteButton.setEnabled( false );
            m_kLoadButton.setEnabled( true );
        } else {
            m_kDeleteButton.setEnabled( true );
            m_kLoadButton.setEnabled( true );
        }
    }

    protected void RefreshList( int nSelected ) {
        File kTargetFolder = new File( kPadData.GetTargetPath() );
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

        m_kListView.SetData( list, kKeyList );
        //m_kListView.SetSelectedIndex( nSelected, false );
    }

    public void Restore( String strRecordName ) {
        //SetWifiActive(false);
        StopPad();
        kPadData.RestoreCurrentStage( strRecordName );
        LaunchPad();
    }

    protected void Restore( View v, int nIndex, boolean bQuestion ) {
        final String strName = m_kListView.GetText( nIndex );

        if ( bQuestion ) {
            DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int which ) {
                    Restore( strName );
                }
            };
            CUiUtility.Question( v, "是否要還原" + strName + "?", OkClick );
        } else {
            Restore( strName );
        }
    }

    public void Save( ) {
        int nCounter = kPadData.ReadCounter();
        ++nCounter;
        kPadData.WriteCounter( nCounter );
        String strName = String.valueOf( nCounter );
        kPadData.RecordCurrentStage( strName );
        RefreshList( 0 );
        RefreshButtonState();
        LaunchPad();
    }

    private void Load( View v, boolean bQuestion ) {
        int nSelectIndex = m_kListView.GetSelectedIndex();
        if ( nSelectIndex == -1 ) {
            return;
        }
        Restore( v, nSelectIndex, bQuestion );
    }

    public void onResume( ) {
        if ( m_nActionIndex == -1 ) {
            return;
        }

        if ( m_nActionIndex == m_nSaveButtonIndex ) {
            Save();
        } else if ( m_nActionIndex == m_nLoadButtonIndex ) {
            Load( m_kActivity.findViewById( android.R.id.content ), true );
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
                        m_kShowBarCheckBox.setChecked( m_kFloatingWindow.ToggleMainVisible() );
                    } else if ( nButtonIndex == m_nTestButtonIndex ) {
                        /*
                        Screen kScreen = m_kRootUtility.GetScreen();
                        CGame.CBoard kBoard = m_kGame.Analysis( kScreen );
                        kBoard.DrawGrid( m_kCanvas);
                        m_kCanvasImageLayer.Show();
                        */
//                        try {
//                            Point kStart = CGame.BoardIndexToScreenPosition(0,0);
//                            Point kSEnd = CGame.BoardIndexToScreenPosition(5,0);
//                            int nOffsetX = 60;
//                            int nOffsetY = 60;
//
//                            sleep( 2000 );
//                            m_kRootUtility.DeviceDownEvent( 600, 1000 );
//                            while ( nStartY < nEndY ) {
//                                sleep( 100 );
//                                nStartY += nOffsetY;
//                                if ( nStartY > nEndY ) {
//                                    nStartY = nEndY;
//                                }
//                                m_kRootUtility.DeviceMoveYToEvent( nStartY );
//                            }
//                            m_kRootUtility.DeviceUpEvent();
//                        }
//                        catch ( InterruptedException e ) {
//
//                        }
                    } else {
                        CUiUtility.LaunchThis( m_kActivity );
                    }
                    m_nActionIndex = nButtonIndex;
                }
            } );

            m_kShowBarCheckBox.setChecked( m_kFloatingWindow.IsVisible() );
        }
    };
}