package com.arenchien.padbackup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.Image;
import android.os.Debug;
import android.util.Log;

import java.nio.ByteBuffer;

public class CGame {

    public static final int Ball_Fire = 0x00000000;
    public static final int Ball_Water = 0x00000001;
    public static final int Ball_Wood = 0x00000002;
    public static final int Ball_Light = 0x00000003;
    public static final int Ball_Dark = 0x00000004;
    public static final int Ball_Heal = 0x00000005;
    public static int Ball_Poison = 0x00000006;
    public static int Ball_HeavyPoison = 0x00000007;
    public static int Ball_Evil = 0x00000008;
    public static int Ball_Plus = 0x00010000;
    public static int Ball_Lock = 0x00020000;

    private static int m_nBallTypeFilter = 0x000000FF;

    public class CBoard {
        CBoard( int nXSize, int nYSize ) {
            m_nXSize = nXSize;
            m_nYSize = nYSize;
            m_aBall = new int[ nXSize * nYSize ];
            for ( int i = 0; i < nXSize * nYSize; ++i ) {
                m_aBall[ i ] = 0x000000FF;
            }
        }

        public int m_nXSize;
        public int m_nYSize;
        public int m_aBall[];

        public void SetBall( int x, int y, int nBall ) {
            m_aBall[ x + y * m_nXSize ] = ( m_aBall[ x + y * m_nXSize ] & ~m_nBallTypeFilter ) | ( nBall & m_nBallTypeFilter );
        }

        public int GetBall( int x, int y ) {
            return m_aBall[ x + y * m_nXSize ] & m_nBallTypeFilter;
        }


        public void DrawGrid( CanvasImageView kCanvas ){
            int nCounter = 0;
            int nCounter2 = 0;
            kCanvas.X = new float[m_nGridXSize*m_nGridYSize*2];
            kCanvas.Y = new float[m_nGridXSize*m_nGridYSize*2];
            kCanvas.PaintColor = new int[m_nGridXSize*m_nGridYSize];
            for ( int x = 0; x < m_nGridXSize; ++x ) {
                for ( int y = 0; y < m_nGridYSize; ++y ) {
                    int nOffsetX = ( m_nGridWidth - m_nValidSize ) / 2;
                    int nOffsetY = ( m_nGridHeight - m_nValidSize ) / 2;
                    int nMinX = x * m_nGridWidth + nOffsetX + m_nGridX;
                    int nMinY = y * m_nGridHeight + nOffsetY + m_nGridY;
                    int nMaxX = ( x + 1 ) * m_nGridWidth - 1 - nOffsetX + m_nGridX;
                    int nMaxY = ( y + 1 ) * m_nGridHeight - 1 - nOffsetY + m_nGridY;

                    switch ( GetBall(x, y) )
                    {
                        case Ball_Fire:kCanvas.PaintColor[nCounter2]=Color.RED;break;
                        case Ball_Water:kCanvas.PaintColor[nCounter2]=Color.BLUE;break;
                        case Ball_Wood:kCanvas.PaintColor[nCounter2]=Color.GREEN;break;
                        case Ball_Light:kCanvas.PaintColor[nCounter2]=Color.YELLOW;break;
                        case Ball_Dark:kCanvas.PaintColor[nCounter2]=Color.BLACK;break;
                        case Ball_Heal:kCanvas.PaintColor[nCounter2]=Color.MAGENTA;break;
                        default:kCanvas.PaintColor[nCounter2]=Color.WHITE;break;
                    }
                    ++nCounter2;

                    kCanvas.X[ nCounter ] = nMinX;
                    kCanvas.Y[ nCounter ] = nMinY;
                    ++nCounter;
                    kCanvas.X[ nCounter ] = nMaxX;
                    kCanvas.Y[ nCounter ] = nMaxY;
                    ++nCounter;


                }
            }
        }
    }

    private static int m_nTitleHeight = 75;
    private static int m_nGridX = 7;
    private static int m_nGridY = 854;
    private static int m_nGridXSize = 6;
    private static int m_nGridYSize = 5;
    private static int m_nGridWidth = 178;
    private static int m_nGridHeight = 178;
    private static int m_nValidSize = 20;
    private static int m_nPlusOffsetLT = 65;
    private static int m_nPlusOffsetRB = 20;

    private static int m_nBallTypeQuantity = 9;
    private static float m_fHueThreshold = 10;
    private static float[] m_aBallHue = { 12, 206, 132, 58, 286, 327, 0, 0, 0 };

    public CBoard Analysis( Screen kScreen ) {
        if ( kScreen == null ) {
            return null;
        }
        if ( ( kScreen.mWidth <= 0 ) || ( kScreen.mHeight <= 0 ) ) {
            return null;
        }
        if ( kScreen.mData == null ) {
            return null;
        }
        if ( ( kScreen.mFormat != PixelFormat.RGBA_8888 ) && ( kScreen.mFormat != PixelFormat.RGBX_8888 ) ) {
            Log.d( "DoraService", "unsupport screen format:" + kScreen.mFormat );
            return null;
        }

        CBoard kBoard = new CBoard( m_nGridXSize, m_nGridYSize );
        //Bitmap kImage = BitmapFactory.decodeFile( strFile );

        //Image.Plane[] kPlane = kInput.getPlanes();
        //ByteBuffer kBuffer = kPlane[ 0 ].getBuffer();


        for ( int x = 0; x < m_nGridXSize; ++x ) {
            for ( int y = 0; y < m_nGridYSize; ++y ) {
                int nOffsetX = ( m_nGridWidth - m_nValidSize ) / 2;
                int nOffsetY = ( m_nGridHeight - m_nValidSize ) / 2;
                int nMinX = x * m_nGridWidth + nOffsetX + m_nGridX;
                int nMinY = y * m_nGridHeight + nOffsetY + m_nGridY;
                int nMaxX = ( x + 1 ) * m_nGridWidth - 1 - nOffsetX + m_nGridX;
                int nMaxY = ( y + 1 ) * m_nGridHeight - 1 - nOffsetY + m_nGridY;

                int nR = 0;
                int nG = 0;
                int nB = 0;
                int nCounter = 0;
                float[] aHsv = new float[ 3 ];
                for ( int xx = nMinX; xx <= nMaxX; ++xx ) {
                    for ( int yy = nMinY; yy <= nMaxY; ++yy ) {
                        int nPixelIndex = ( yy * kScreen.mWidth + xx ) * 4;
                        nR += kScreen.mData[ nPixelIndex ] & 0xFF;
                        nG += kScreen.mData[ nPixelIndex + 1 ] & 0xFF;
                        nB += kScreen.mData[ nPixelIndex + 2 ] & 0xFF;
                        ++nCounter;
                    }
                }
                nR /= nCounter;
                nG /= nCounter;
                nB /= nCounter;

                Color.RGBToHSV( nR, nG, nB, aHsv );
                float fH = aHsv[ 0 ];
                //float fS = aHsv[ 1 ];
                //float fV = aHsv[ 2 ];
                for ( int i = 0; i < m_nBallTypeQuantity; ++i ) {
                    if ( Math.abs( fH - m_aBallHue[ i ] ) < m_fHueThreshold ) {
                        kBoard.SetBall( x, y, i );
                        break;
                    }
                }
            }
        }
        return kBoard;
    }
/*
    private ColorIndex Analysis( Screen kScreen ) {
        if ( kScreen == null ) {
            return null;
        }
        if ( ( kScreen.mWidth <= 0 ) || ( kScreen.mHeight <= 0 ) ) {
            return null;
        }
        if ( kScreen.mData == null ) {
            return null;
        }
        if ( ( kScreen.mFormat != 1 ) && ( kScreen.mFormat != 2 ) && ( kScreen.mFormat != 5 ) ) {
            Debug.d( "DoraService", "unsupport screen format:" + kScreen.mFormat );
            return null;
        }

        LoadCorrectPosition( paramGameConfig );
        ScreenInfo localScreenInfo1;
        if ( ( this.mTableStartx == -1 ) || ( this.mTableStarty == -1 ) || ( this.mTableWidth == -1 ) || ( this.mTableHeight == -1 ) || ( this.mTableStarty + this.mTableHeight > this.mScreenRealSize.y ) || ( this.mTableStartx + this.mTableWidth > this.mScreenRealSize.x ) ) {
            ScreenInfo localScreenInfo2 = paramGameConfig.getScreenInfo( this.mScreenSize.x, this.mScreenSize.y );
            localScreenInfo1 = localScreenInfo2;
            if ( localScreenInfo2 == null ) {
                paramGameConfig = new Message();
                paramGameConfig.what = 1;
                paramGameConfig.obj = getString( 2131165332 );
                this.mDoraHandler.sendMessage( paramGameConfig );
                return null;
            }
        } else {
            localScreenInfo1 = new ScreenInfo( kScreen.mWidth, kScreen.mHeight, this.mTableStartx, this.mTableStarty, this.mTableStartx + this.mTableWidth, this.mTableStarty + this.mTableHeight );
        }
        Debug.d( "DoraService", "screen width:" + kScreen.mWidth );
        Debug.d( "DoraService", "screen width:" + kScreen.mHeight );
        Debug.d( "DoraService", "screen sx:" + localScreenInfo1.mStartX );
        Debug.d( "DoraService", "screen sy:" + localScreenInfo1.mStartY );
        Debug.d( "DoraService", "screen ex:" + localScreenInfo1.mEndX );
        Debug.d( "DoraService", "screen ey:" + localScreenInfo1.mEndY );
        paramGameConfig = getColorsIndexFromArray( paramGameConfig, kScreen.mData, kScreen.mFormat, kScreen.mWidth, kScreen.mHeight, localScreenInfo1.mStartX, localScreenInfo1.mStartY, localScreenInfo1.mEndX, localScreenInfo1.mEndY, paramBoolean );
        Debug.d( "DoraService", "mIsReady:" + paramGameConfig.mIsReady );
        return paramGameConfig;

    }
    */
}