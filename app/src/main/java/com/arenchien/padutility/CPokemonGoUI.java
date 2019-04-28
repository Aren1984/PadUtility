package com.arenchien.padutility;

import android.app.Activity;
import android.content.DialogInterface;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by Aren on 2018/10/10.
 */

public class CPokemonGoUI {
    Activity m_kActivity;
    Button m_kLoadButton;
    Button m_kSaveButton;
    RadioButton m_kAccount000;
    RadioButton m_kAccount001;
    RadioButton m_kAccount002;
    RadioButton m_kAccount003;
    RadioButton m_kAccount004;

    String m_strPackageName = "com.nianticlabs.pokemongo";
    String m_strRoot = "/sdcard/Android/data/com.nianticlabs.pokemongo";
    String m_strAccount = "/.01Account";

    String m_kCopyFolder[] = new String[ 3 ];

    public CPokemonGoUI() {
        m_kCopyFolder[ 0 ] = "/shared_prefs";
        m_kCopyFolder[ 1 ] = "/files/il2cpp";
        m_kCopyFolder[ 2 ] = "/files/remote_config_cache";
    }

    public void onCreate( Activity kActivity ) {
        m_kActivity = kActivity;
        m_kLoadButton = ( Button ) m_kActivity.findViewById( R.id.ButtonPokemonLoad );
        m_kSaveButton = ( Button ) m_kActivity.findViewById( R.id.ButtonPokemonSave );
        m_kAccount000 = ( RadioButton ) m_kActivity.findViewById( R.id.AccountRadioButton000 );
        m_kAccount001 = ( RadioButton ) m_kActivity.findViewById( R.id.AccountRadioButton001 );
        m_kAccount002 = ( RadioButton ) m_kActivity.findViewById( R.id.AccountRadioButton002 );
        m_kAccount003 = ( RadioButton ) m_kActivity.findViewById( R.id.AccountRadioButton003 );
        m_kAccount004 = ( RadioButton ) m_kActivity.findViewById( R.id.AccountRadioButton004 );

        m_kLoadButton.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Load();
            }
        } );
        m_kSaveButton.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick( View v ) {
                DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int which ) {
                        Save();
                    }
                };
                CUiUtility.Question( v, "是否要覆蓋?", OkClick );
            }
        } );

        m_strSettingsFile = CPadData.GetTargetPath() + "/CPokemonGoUI.log" ;
        ReadSettings();
        SetActiveAccount();
    }

    protected String GetPackageName() {
        return m_strPackageName;
    }

    protected String GetActiveAccount() {
        String strAccount = m_strRoot + m_strAccount;
        if ( m_kAccount000.isChecked() ) {
            strAccount += "/000 Su";
            m_nSelected = 0;
        } else if ( m_kAccount001.isChecked() ) {
            strAccount += "/001 Blue";
            m_nSelected = 1;
        } else if ( m_kAccount002.isChecked() ) {
            strAccount += "/002 Red";
            m_nSelected = 2;
        } else if ( m_kAccount003.isChecked() ) {
            strAccount += "/003 Blue";
            m_nSelected = 3;
        } else if ( m_kAccount004.isChecked() ) {
            strAccount += "/004 Blue";
            m_nSelected = 4;
        } else {

        }

        WriteSettings();
        return strAccount;
    }

    protected void SetActiveAccount() {
        if ( m_nSelected == 0 ) {
            m_kAccount000.setChecked( true );
        } else if ( m_nSelected == 1 ) {
            m_kAccount001.setChecked( true );
        } else if ( m_nSelected == 2 ) {
            m_kAccount002.setChecked( true );
        } else if ( m_nSelected == 3 ) {
            m_kAccount003.setChecked( true );
        } else if ( m_nSelected == 4 ) {
            m_kAccount004.setChecked( true );
        } else {

        }
    }

    protected void DeleteFiles( String strRoot ) {
        for ( int i = 0; i < m_kCopyFolder.length; ++i ) {
            String strSubFolderFolder = strRoot + m_kCopyFolder[ i ];
            File kFolder = new File( strSubFolderFolder );
            CFileUtility.DeleteAllInFolder( kFolder );
        }
    }

    protected void CopyFiles( String strSource, String strTarget ) {
        for ( int i = 0; i < m_kCopyFolder.length; ++i ) {
            String strSourceFolderFolder = strSource + m_kCopyFolder[ i ];
            String strTargetFolderFolder = strTarget + m_kCopyFolder[ i ];
            File kSourceFolder = new File( strSourceFolderFolder );
            File kTargetFolder = new File( strTargetFolderFolder );
            CFileUtility.CopyDirectory( kSourceFolder, kTargetFolder );
        }
    }

    protected void Load() {
        StopPGo();

        String strAccount = GetActiveAccount();
        String strTargetFolder = m_strRoot;
        DeleteFiles( strTargetFolder );
        CopyFiles( strAccount, strTargetFolder );

        LaunchPGo();
    }

    protected void Save() {
        //CUiUtility.LaunchThis( m_kActivity );

        String strAccount = GetActiveAccount();
        String strTargetFolder = m_strRoot;
        DeleteFiles( strAccount );
        CopyFiles( strTargetFolder, strAccount );

        //LaunchPGo();
    }

    protected void LaunchPGo() {
        CUiUtility.LaunchApp( m_kActivity, GetPackageName() );
    }

    private void StopPGo() {
        CUiUtility.StopApp( m_kActivity, GetPackageName() );
    }

    private String m_strSettingsFile;
    private int m_nSelected = 1;
    public void ReadSettings() {
        try {
            File kFile = new File( m_strSettingsFile );
            InputStream kInput = new FileInputStream( kFile );

            byte[] kBuffer = new byte[ 4 ];
            kInput.read( kBuffer );
            ByteBuffer wrapped = ByteBuffer.wrap( kBuffer );
            m_nSelected = wrapped.getInt();
            kInput.close();
        } catch ( IOException ex ) {
        }
    }

    public void WriteSettings() {
        try {
            File kFile = new File( m_strSettingsFile );
            OutputStream kOutput = new FileOutputStream( kFile );
            byte[] kBuffer = ByteBuffer.allocate( 4 ).putInt( m_nSelected ).array();
            kOutput.write( kBuffer );
            kOutput.close();
        } catch ( IOException ex ) {
        }
    }
}