package com.arenchien.padutility;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by Aren on 2017/6/25.
 */

public class CanvasImageView extends ImageView {
    public CanvasImageView( Context context ) {
        super( context );
        // TODO Auto-generated constructor stub
    }

    public float X[] = {};
    public float Y[] = {};
    public int PaintColor[] = {};

    @Override
    protected void onDraw( Canvas canvas ) {
        super.onDraw( canvas );
        Paint p = new Paint( Paint.ANTI_ALIAS_FLAG );
        p.setColor( Color.RED );
        for ( int i = 0; i < X.length; i += 2) {
            p.setColor( PaintColor[(int)(i/2)] );
            canvas.drawRect( X[ i ], Y [i ]-75, X[ i + 1 ], Y[i+1]-75, p );
        }
    }

}