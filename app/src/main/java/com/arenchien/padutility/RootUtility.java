package com.arenchien.padutility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RootUtility {
    private static final String TAG = "RootUtil";
    private static final String[] mBinaryPath = { "/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/", "/system/bin/.ext/" };
    private Process mShellWapper = null;
    private DataInputStream m_kReader = null;
    private DataOutputStream m_kWriter = null;

    public RootUtility( ) throws IOException {
        String paramString = "su";
        try {
            mShellWapper = Runtime.getRuntime().exec( paramString );
            m_kReader = new DataInputStream( mShellWapper.getInputStream() );
            m_kWriter = new DataOutputStream( mShellWapper.getOutputStream() );
        }
        catch ( IOException localIOException1 ) {
            mShellWapper = null;
            throw new IOException( "Cannot execute " + paramString );
        }
    }

    public void destroy( ) {
        if ( mShellWapper == null || m_kWriter == null ) {
            return;
        }
        try {
            m_kWriter.writeBytes( "exit \n" );
            m_kWriter.flush();
            m_kWriter.close();
            m_kWriter = null;
            m_kReader.close();
            m_kReader = null;
            mShellWapper.destroy();
            mShellWapper = null;
        }
        catch ( IOException localIOException ) {

        }
    }

    private int ByteArrayToInt( byte[] kSource ) {
        return ( ( ( 0x0 | kSource[ 3 ] & 0xFF ) << 8 | kSource[ 2 ] & 0xFF ) << 8 | kSource[ 1 ] & 0xFF ) << 8 | kSource[ 0 ] & 0xFF;
    }

    public Screen GetScreen( ) {
        if ( m_kReader == null || m_kWriter == null ) {
            return null;
        }

        try {
            int nSkipSize = m_kReader.available();
            m_kReader.skipBytes( nSkipSize );
            m_kWriter.writeBytes( "sh -c screencap \n" );
            m_kWriter.flush();
            Screen kScreen = new Screen();
            Object localObject1 = new byte[ 4 ];
            m_kReader.readFully( ( byte[] ) localObject1 );
            kScreen.mWidth = ByteArrayToInt( ( byte[] ) localObject1 );
            m_kReader.readFully( ( byte[] ) localObject1 );
            kScreen.mHeight = ByteArrayToInt( ( byte[] ) localObject1 );
            m_kReader.readFully( ( byte[] ) localObject1 );
            kScreen.mFormat = ByteArrayToInt( ( byte[] ) localObject1 );
            if ( ( kScreen.mWidth <= 0 ) || ( kScreen.mHeight <= 0 ) ) {
                return null;
            }
            kScreen.mData = new byte[ kScreen.mWidth * kScreen.mHeight * 4 ];
            m_kReader.readFully( kScreen.mData );
            return kScreen;
        }
        catch ( Exception localException ) {

        }
        return null;
    }

}