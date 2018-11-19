package com.arenchien.padutility;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.view.View;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by ArenChien on 2018/10/9.
 */

public class CUiUtility {
    static String m_strPackageNameThis = "com.example.arenchien.padbackup";

    public static void Question( View v, String strQuestion, DialogInterface.OnClickListener kOkAction ) {
        AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder( v.getContext() );
        MyAlertDialog.setTitle( "Warning" );
        MyAlertDialog.setMessage( strQuestion );
        DialogInterface.OnClickListener CancelClick = new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface kDialog, int nWhich ) {
            }
        };
        MyAlertDialog.setPositiveButton( "OK", kOkAction );
        MyAlertDialog.setNeutralButton( "Cancel", CancelClick );
        MyAlertDialog.show();
    }

    public static void LaunchApp( Activity kActivity, String strPackageName ) {
        Intent launchIntent = kActivity.getPackageManager().getLaunchIntentForPackage( strPackageName );
        kActivity.startActivity( launchIntent );
    }

    public static void LaunchThis( Activity kActivity ) {
        Intent launchIntent = kActivity.getPackageManager().getLaunchIntentForPackage( m_strPackageNameThis );
        launchIntent.setFlags( /*Intent.FLAG_ACTIVITY_NO_ANIMATION | */Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
        kActivity.startActivity( launchIntent );
    }

    public static void StopApp( Activity kActivity, String strPackageName ) {
        ActivityManager am = (ActivityManager) kActivity.getSystemService( ACTIVITY_SERVICE );
        if ( am != null ) {
            try {
                am.killBackgroundProcesses( strPackageName );
            } catch ( Exception e ) {
                String str = e.getMessage();
            }
        }
    }
/*
    private void SetWifiActive( boolean bActive ) {
        WifiManager wifiManager = (WifiManager) this.getSystemService( Context.WIFI_SERVICE );
        wifiManager.setWifiEnabled( bActive );
    }

    private boolean GetWifiActive() {
        WifiManager wifiManager = (WifiManager) this.getSystemService( Context.WIFI_SERVICE );
        return wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
    }
*/
}
