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

/**
 * Created by Aren on 2018/10/10.
 */

public class CPokemonGoUI {
    Activity m_kActivity;
    Button m_kLoadButton;
    Button m_kSaveButton;
    RadioButton m_kAccount001;
    RadioButton m_kAccount002;
    RadioButton m_kAccount003;

    String m_strPackageName = "com.nianticlabs.pokemongo";
    String m_strRoot = "/sdcard/Android/data/com.nianticlabs.pokemongo";
    String m_strAccount = "/.01Account";

    String m_kCopyFolder[] = new String[ 3 ];

    public CPokemonGoUI() {
        m_kCopyFolder[ 0 ] = "/shared_prefs";
        m_kCopyFolder[ 1 ] = "/files/il2cpp";
        m_kCopyFolder[ 2 ] = "/files/remote_config_cache";
        //m_kCopyFolder[ 3 ] = "/files/DiskCache";
        //m_kCopyFolder[ 4 ] = "/cache";
    }

    public void onCreate( Activity kActivity ) {
        m_kActivity = kActivity;
        m_kLoadButton = ( Button ) m_kActivity.findViewById( R.id.ButtonPokemonLoad );
        m_kSaveButton = ( Button ) m_kActivity.findViewById( R.id.ButtonPokemonSave );
        m_kAccount001 = ( RadioButton ) m_kActivity.findViewById( R.id.AccountRadioButton001 );
        m_kAccount002 = ( RadioButton ) m_kActivity.findViewById( R.id.AccountRadioButton002 );
        m_kAccount003 = ( RadioButton ) m_kActivity.findViewById( R.id.AccountRadioButton003 );

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
    }

    protected String GetPackageName() {
        return m_strPackageName;
    }

    protected String GetActiveAccount() {
        String strAccount = m_strRoot + m_strAccount;
        if ( m_kAccount001.isChecked() ) {
            strAccount += "/001 Blue";
        } else if ( m_kAccount002.isChecked() ) {
            strAccount += "/002 Red";
        } else if ( m_kAccount003.isChecked() ) {
            strAccount += "/003 Blue";
        } else {

        }
        return strAccount;
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
}