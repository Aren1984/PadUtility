package com.arenchien.padutility;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

/**
 * Created by ArenChien on 2018/10/8.
 */

public class CPadData {
    String m_strJapan = "/sdcard/.001.pad";
    String m_strTw = "/sdcard/.002.padtw";
    String m_strSourceFolder = "/files";
    String m_strTargetFolder = "/backup";
    String m_strPackageNameJp = "jp.gungho.pad";
    String m_strPackageNameTw = "jp.gungho.padHT";

    private boolean IsJapan() {
        //RadioButton kRadioButton = ( RadioButton )findViewById( R.id.JapanRadioButton );
        return false;//kRadioButton.isChecked();
    }

    public String GetSourcePath() {
        if ( IsJapan() ) {
            return m_strJapan + m_strSourceFolder;
        } else {
            return m_strTw + m_strSourceFolder;
        }
    }

    public String GetTargetPath() {
        if ( IsJapan() ) {
            return m_strJapan + m_strTargetFolder;
        } else {
            return m_strTw + m_strTargetFolder;
        }
    }

    public String GetPackageName() {
        if ( IsJapan() ) {
            return m_strPackageNameJp;
        } else {
            return m_strPackageNameTw;
        }
    }

    public void ClearRecord() {
        File kTargetFolder = new File( GetTargetPath() );
        File[] kSubFiles = kTargetFolder.listFiles();
        for ( int i = 0; kSubFiles != null && i < kSubFiles.length; ++i ) {
            if ( kSubFiles[ i ].isDirectory() ) {
                CFileUtility.DeleteFolder( kSubFiles[ i ] );
            } else {
                kSubFiles[ i ].delete();
            }
        }
    }

    public void RecordCurrentStage( String strName ) {
        String strTargetFolder = GetTargetPath() + "/" + strName;
        File kTargetFolder = new File( strTargetFolder );
        kTargetFolder.mkdir();
        File kSourceFolder = new File( GetSourcePath() );
        File[] kSubFiles = kSourceFolder.listFiles();
        for ( int i = 0; kSubFiles != null && i < kSubFiles.length; ++i ) {
            if ( kSubFiles[ i ].isFile() ) {
                File kTarget = new File( strTargetFolder + "/" + kSubFiles[ i ].getName() );
                CFileUtility.CopyFile( kSubFiles[ i ], kTarget );
            }
        }
    }

    public void RestoreCurrentStage( String strName ) {
        String strTargetFolder = GetTargetPath() + "/" + strName;
        File kTargetFolder = new File( strTargetFolder );
        File kSourceFolder = new File( GetSourcePath() );
        File[] kSubFiles = kTargetFolder.listFiles();
        for ( int i = 0; kSubFiles != null && i < kSubFiles.length; ++i ) {
            if ( kSubFiles[ i ].isFile() ) {
                File kTarget = new File( kSourceFolder + "/" + kSubFiles[ i ].getName() );
                CFileUtility.CopyFile( kSubFiles[ i ], kTarget );
            }
        }
    }

    public void ClearAccount() {
        CFileUtility.DeleteFilesInFolder( new File( GetSourcePath() ) );
    }

    public void DeleteRecord( String strName ) {
        String strTargetFolder = GetTargetPath() + "/" + strName;
        File kTargetFolder = new File( strTargetFolder );
        CFileUtility.DeleteFolder( kTargetFolder );
    }

    public int ReadCounter() {
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

    public void WriteCounter( int nCounter ) {
        try {
            File kFile = new File( GetTargetPath() + "/counter.log" );
            OutputStream kOutput = new FileOutputStream( kFile );
            byte[] kBuffer = ByteBuffer.allocate( 4 ).putInt( nCounter ).array();
            kOutput.write( kBuffer );
            kOutput.close();
        } catch ( IOException ex ) {
        }
    }







}