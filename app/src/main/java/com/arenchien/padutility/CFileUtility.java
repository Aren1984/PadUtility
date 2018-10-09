package com.arenchien.padutility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ArenChien on 2018/10/8.
 */

public class CFileUtility {
    public static void CopyFile( File kSource, File kTarget ) {
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

    public static void DeleteFolder( File kFolder ) {
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

    public static void DeleteFilesInFolder( File kFolder ) {
        File[] kSubFiles = kFolder.listFiles();
        for ( int i = 0; kSubFiles != null && i < kSubFiles.length; ++i ) {
            if ( kSubFiles[ i ].isFile() ) {
                kSubFiles[ i ].delete();
            }
        }
    }
}
