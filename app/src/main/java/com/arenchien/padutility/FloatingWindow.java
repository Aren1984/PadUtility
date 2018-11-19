package com.arenchien.padutility;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Binder;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class FloatingWindow extends Service {
    private interface OnMovingListener {
        void OnMove( View v, int nOffsetX, int nOffsetY );
    }

    private interface OnClickListener {
        void OnClicked( View v );
    }

    public interface OnButtonClickedListener {
        void OnButtonClicked( View v, int nButtonIndex );
    }

    public void SetButtonClickedListener( OnButtonClickedListener fnButtonClickedListener ) {
        m_fnButtonClickedListener = fnButtonClickedListener;
    }

    // Binder
    public class LocalBinder extends Binder {
        FloatingWindow getService() {
            return FloatingWindow.this;
        }
    }

    private final IBinder m_kBinder = new LocalBinder();

    @Override
    public IBinder onBind( Intent intent ) {
        return m_kBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        m_kWindowManager = (WindowManager) getSystemService( WINDOW_SERVICE );

        m_kMainIcon = new CImageLayer( this, R.drawable.main_open, m_nMainIconWidth, m_nIconSize, 0, 200, true, true );

        m_kMainIcon.SetClickListener( new OnClickListener() {
            @Override
            public void OnClicked( View v ) {
                m_bShowAction = !m_bShowAction;
                if ( m_bShowAction ) {
                    for ( int i = 0; i < m_kButtons.size(); ++i ) {
                        m_kButtons.get( i ).Show();
                    }
                    m_kMainIcon.SetImage( R.drawable.main_close );
                } else {
                    for ( int i = 0; i < m_kButtons.size(); ++i ) {
                        m_kButtons.get( i ).Hide();
                    }
                    m_kMainIcon.SetImage( R.drawable.main_open );
                }
            }
        } );
        m_kMainIcon.SetMovingListener( new OnMovingListener() {
            @Override
            public void OnMove( View v, int nOffsetX, int nOffsetY ) {
                for ( int i = 0; i < m_kButtons.size(); ++i ) {
                    m_kButtons.get( i ).Move( nOffsetX, nOffsetY );
                }
            }
        } );

        WindowManager wm = (WindowManager) getSystemService( Context.WINDOW_SERVICE );
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize( size );
        m_nScreenWidth = size.y;
    }

    @Override
    public void onDestroy() {
        for ( int i = 0; i < m_kButtons.size(); ++i ) {
            m_kButtons.get( i ).Destroy();
        }
        m_kMainIcon.Destroy();
        super.onDestroy();
    }

    public int AddButton( int nResourceId ) {
        int nOffset = m_kButtons.size();
        int nX = m_kMainIcon.m_kParameter.x + m_nIconSize * nOffset + m_nMainIconWidth;
        int nY = m_kMainIcon.m_kParameter.y;
        m_nMaxWidth += m_nIconSize;
        CImageLayer kButton = new CImageLayer( this, nResourceId, m_nIconSize, m_nIconSize, nX, nY, false, false );
        m_kButtons.add( kButton );
        final int nIndex = m_kButtons.size() - 1;
        kButton.SetClickListener( new OnClickListener() {
            @Override
            public void OnClicked( View v ) {
                if ( m_fnButtonClickedListener != null ) {
                    m_fnButtonClickedListener.OnButtonClicked( v, nIndex );
                }
            }
        } );

        if ( m_bShowAction ) {
            kButton.Show();
        }

        return nIndex;
    }

    public void RemoveButton( int nButtonIndex ) {
        m_nMaxWidth -= m_nIconSize;
        CImageLayer kButton = m_kButtons.get( nButtonIndex );
        if ( m_bShowAction ) {
            kButton.Hide();
        }
        m_kButtons.remove( nButtonIndex );
    }

    public boolean ToggleMainVisible() {
        if ( m_bShowMain ) {
            m_kMainIcon.Hide();
            for ( int i = 0; i < m_kButtons.size(); ++i ) {
                m_kButtons.get( i ).Hide();
            }
            m_bShowMain = false;
        } else {
            m_kMainIcon.Show();
            if ( m_bShowAction ) {
                for ( int i = 0; i < m_kButtons.size(); ++i ) {
                    m_kButtons.get( i ).Show();
                }
            }
            m_bShowMain = true;
        }
        return m_bShowMain;
    }

    private WindowManager m_kWindowManager;
    private CImageLayer m_kMainIcon;
    private List<CImageLayer> m_kButtons = new ArrayList<CImageLayer>();
    private boolean m_bShowMain = true;
    private boolean m_bShowAction = false;
    private int m_nIconSize = 128;
    private int m_nMainIconWidth = 64;
    private int m_nMaxWidth = m_nMainIconWidth;
    private OnButtonClickedListener m_fnButtonClickedListener;
    private int m_nScreenWidth = 0;

    public class CImageLayer {
        private ImageView m_kImage;
        private WindowManager.LayoutParams m_kParameter;
        private boolean m_bEnableDrag;
        private boolean m_bVisible;

        public void SetMovingListener( OnMovingListener fnMovingListener ) {
            m_fnMovingListener = fnMovingListener;
        }

        public void SetClickListener( OnClickListener fnClickListener ) {
            m_fnClickListener = fnClickListener;
        }

        public WindowManager.LayoutParams Parameter() {
            return m_kParameter;
        }

        private OnMovingListener m_fnMovingListener;
        private OnClickListener m_fnClickListener;

        public CImageLayer( Context kContext, int nResourceId, int nWidth, int nHeight, int nX, int nY, boolean bVisible, boolean bEnableDrag ) {
            m_kImage = new ImageView( kContext );
            m_kImage.setImageResource( nResourceId );
            m_kParameter = new WindowManager.LayoutParams( nWidth, nHeight, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT );
            m_kParameter.gravity = Gravity.TOP | Gravity.LEFT;
            m_kParameter.x = nX;
            m_kParameter.y = nY;
            m_bVisible = bVisible;
            m_bEnableDrag = bEnableDrag;

            m_kImage.setOnTouchListener( new View.OnTouchListener() {
                private int m_fDownX;
                private int m_fDownY;
                private float m_fLastX;
                private float m_fLastY;

                @Override
                public boolean onTouch( View v, MotionEvent event ) {
                    switch ( event.getAction() ) {
                        case MotionEvent.ACTION_DOWN:
                            m_fDownX = m_kParameter.x;
                            m_fDownY = m_kParameter.y;
                            m_fLastX = event.getRawX();
                            m_fLastY = event.getRawY();
                            return true;
                        case MotionEvent.ACTION_UP:
                            if ( m_fDownX == m_kParameter.x && m_fDownY == m_kParameter.y && m_fnClickListener != null ) {
                                m_fnClickListener.OnClicked( v );
                            }
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            if ( m_bEnableDrag ) {
                                int nOffsetX = (int) ( event.getRawX() - m_fLastX );
                                int nOffsetY = (int) ( event.getRawY() - m_fLastY );
                                m_kParameter.x += nOffsetX;
                                m_kParameter.y += nOffsetY;
                                if ( m_kParameter.x < 0 ) {
                                    nOffsetX -= m_kParameter.x;
                                    m_kParameter.x = 0;
                                }
                                if ( m_kParameter.y < 0 ) {
                                    nOffsetY -= m_kParameter.y;
                                    m_kParameter.y = 0;
                                }
                                if ( m_kParameter.x + m_nMaxWidth > m_nScreenWidth ) {
                                    m_kParameter.x = m_nScreenWidth - m_nMaxWidth;
                                }
                                m_kWindowManager.updateViewLayout( m_kImage, m_kParameter );
                                if ( m_fnMovingListener != null ) {
                                    m_fnMovingListener.OnMove( v, nOffsetX, nOffsetY );
                                }
                                m_fLastX = event.getRawX();
                                m_fLastY = event.getRawY();
                            }
                            return true;
                    }
                    return false;
                }
            } );

            if ( m_bVisible ) {
                m_kWindowManager.addView( m_kImage, m_kParameter );
            }
        }

        public CImageLayer( ImageView kImage, int nWidth, int nHeight, int nX, int nY, boolean bVisible ) {
            m_kImage = kImage;
            m_kParameter = new WindowManager.LayoutParams( nWidth, nHeight, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT );
            m_kParameter.gravity = Gravity.TOP | Gravity.LEFT;
            m_kParameter.x = nX;
            m_kParameter.y = nY;
            m_bVisible = bVisible;
            m_bEnableDrag = false;

            m_kImage.setOnTouchListener( new View.OnTouchListener() {
                @Override
                public boolean onTouch( View v, MotionEvent event ) {
                    Hide();
                    return true;
                }
            } );

            if ( m_bVisible ) {
                m_kWindowManager.addView( m_kImage, m_kParameter );
            }
        }

        protected void Destroy() {
            if ( m_bVisible ) {
                m_kWindowManager.removeView( m_kImage );
            }
        }

        public void Move( int nOffsetX, int nOffsetY ) {
            m_kParameter.x += nOffsetX;
            m_kParameter.y += nOffsetY;
            if ( m_bVisible ) {
                m_kWindowManager.updateViewLayout( m_kImage, m_kParameter );
            }
        }

        public void Show() {
            if ( !m_bVisible ) {
                m_kWindowManager.addView( m_kImage, m_kParameter );
                m_bVisible = true;
            }
        }

        public void Hide() {
            if ( m_bVisible ) {
                m_kWindowManager.removeView( m_kImage );
                m_bVisible = false;
            }
        }

        public void SetImage( int nResourceId ) {
            m_kImage.setImageResource( nResourceId );
        }
    }


    /*
        public class CBoardPanel {
            private ImageView m_kImage;
            private WindowManager.LayoutParams m_kParameter;

            public void SetMovingListener( OnMovingListener fnMovingListener ) {
                m_fnMovingListener = fnMovingListener;
            }

            public void SetClickListener( OnClickListener fnClickListener ) {
                m_fnClickListener = fnClickListener;
            }

            public WindowManager.LayoutParams Parameter() {
                return m_kParameter;
            }

            private OnMovingListener m_fnMovingListener;
            private OnClickListener m_fnClickListener;
            private int m_nGridX = 7;
            private int m_nGridY = 854;
            private int m_nGridXSize = 6;
            private int m_nGridYSize = 5;
            private int m_nGridWidth = 178;
            private int m_nGridHeight = 178;

            private void UpdateParametere() {
                m_kParameter.x = m_nGridX;
                m_kParameter.y = m_nGridY;
                m_kParameter.width = m_nGridXSize * m_nGridWidth;
                m_kParameter.height = m_nGridYSize * m_nGridHeight;

            }

            public CBoardPanel( Context kContext ) {
                m_kImage = new ImageView( kContext );
                m_kImage.setImageResource( nResourceId );
                m_kParameter = new WindowManager.LayoutParams( 1, 1, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT );
                m_kParameter.gravity = Gravity.TOP | Gravity.LEFT;
                UpdateParametere();

                m_kImage.setOnTouchListener( new View.OnTouchListener() {
                    private int m_fDownX;
                    private int m_fDownY;
                    private float m_fLastX;
                    private float m_fLastY;

                    @Override
                    public boolean onTouch( View v, MotionEvent event ) {
                        switch ( event.getAction() ) {
                            case MotionEvent.ACTION_DOWN:
                                m_fDownX = m_kParameter.x;
                                m_fDownY = m_kParameter.y;
                                m_fLastX = event.getRawX();
                                m_fLastY = event.getRawY();
                                return true;
                            case MotionEvent.ACTION_UP:
                                if ( m_fDownX == m_kParameter.x && m_fDownY == m_kParameter.y && m_fnClickListener != null ) {
                                    m_fnClickListener.OnClicked( v );
                                }
                                return true;
                            case MotionEvent.ACTION_MOVE:
                                if ( m_bEnableDrag ) {
                                    int nOffsetX = ( int ) ( event.getRawX() - m_fLastX );
                                    int nOffsetY = ( int ) ( event.getRawY() - m_fLastY );
                                    m_kParameter.x += nOffsetX;
                                    m_kParameter.y += nOffsetY;
                                    if ( m_kParameter.x < 0 ) {
                                        nOffsetX -= m_kParameter.x;
                                        m_kParameter.x = 0;
                                    }
                                    if ( m_kParameter.y < 0 ) {
                                        nOffsetY -= m_kParameter.y;
                                        m_kParameter.y = 0;
                                    }
                                    m_kWindowManager.updateViewLayout( m_kImage, m_kParameter );
                                    if ( m_fnMovingListener != null ) {
                                        m_fnMovingListener.OnMove( v, nOffsetX, nOffsetY );
                                    }
                                    m_fLastX = event.getRawX();
                                    m_fLastY = event.getRawY();
                                }
                                return true;
                        }
                        return false;
                    }
                } );

                if ( m_bVisible ) {
                    m_kWindowManager.addView( m_kImage, m_kParameter );
                }
            }

    protected void Destroy() {
        if ( m_bVisible ) {
            m_kWindowManager.removeView( m_kImage );
        }
    }

    public void Move( int nOffsetX, int nOffsetY ) {
        m_kParameter.x += nOffsetX;
        m_kParameter.y += nOffsetY;
        if ( m_bVisible ) {
            m_kWindowManager.updateViewLayout( m_kImage, m_kParameter );
        }
    }

    public void Show() {
        if ( !m_bVisible ) {
            m_kWindowManager.addView( m_kImage, m_kParameter );
            m_bVisible = true;
        }
    }

    public void Hide() {
        if ( m_bVisible ) {
            m_kWindowManager.removeView( m_kImage );
            m_bVisible = false;
        }
    }

    public void SetImage( int nResourceId ) {
        m_kImage.setImageResource( nResourceId );
    }
}
*/

}