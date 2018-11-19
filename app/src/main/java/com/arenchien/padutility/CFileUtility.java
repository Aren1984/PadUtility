package com.arenchien.padutility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

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
        }
        catch ( IOException ex ) {

        }
    }

    public static void CopyDirectory( File sourceLocation, File targetLocation ) {

        if ( sourceLocation.isDirectory() ) {
            if ( !targetLocation.exists() ) {
                targetLocation.mkdirs();
            }

            String[] children = sourceLocation.list();
            for ( int i = 0; i < children.length; i++ ) {
                CopyDirectory( new File( sourceLocation, children[ i ] ), new File( targetLocation, children[ i ] ) );
            }
        } else {

            CopyFile( sourceLocation, targetLocation );
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

    public static void DeleteAllInFolder( File kFolder ) {
        File[] kSubFiles = kFolder.listFiles();
        for ( int i = 0; kSubFiles != null && i < kSubFiles.length; ++i ) {
            if ( kSubFiles[ i ].isFile() ) {
                kSubFiles[ i ].delete();
            } else if ( kSubFiles[ i ].isDirectory() ) {
                kSubFiles[ i ].delete();
            }
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
}