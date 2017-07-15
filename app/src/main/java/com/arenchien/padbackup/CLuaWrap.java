package com.arenchien.padbackup;

import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * Created by Aren Chien on 2016/4/5.
 */
public class CLuaWrap {

    public static class FindImage extends OneArgFunction {

        @Override
        public LuaValue call( LuaValue arg1 ) {
            /*
            boolean ret = true;
            LuaString imageId = arg1.checkstring();
            IplImage screen = OpenCVUtils.createFromBufferedImage( JavaMonkey.getInstance().takeSnapshot().getBufferedImage() );
            IplImage template = cvLoadImage( CommonUtils.getFileName( imageId.toString() ) );
            boolean ret = OpenCVUtils.findImage( screen, template );
            screen.release();
            cvReleaseImage( template );
            return ( ret ) ? LuaBoolean.TRUE : LuaBoolean.FALSE;
            */
            //MonkeyImage kImage = MonkeyDevice.takeSnapshot();
            return LuaBoolean.FALSE;
        }
    }
}