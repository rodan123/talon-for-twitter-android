package com.klinker.android.twitter_l.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

import com.klinker.android.twitter_l.R;

public class PermissionModelUtils {

    public static final String[] NECESSARY_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final String PERMISSION_CHECK_PREF = "marshmallow_permission_check";

    private Context context;
    private SharedPreferences sharedPrefs;

    public PermissionModelUtils(Context context) {
        this.context = context;
        this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean needPermissionCheck() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        } else {
            return sharedPrefs.getBoolean(PERMISSION_CHECK_PREF, true);
        }
    }

    public void showPermissionExplanationThenAuthorization() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.permission_check_title)
                .setMessage(R.string.permission_check_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions();
                        sharedPrefs.edit().putBoolean(PERMISSION_CHECK_PREF, false).commit();
                    }
                })
                .setCancelable(false)
                .create().show();
    }

    private void requestPermissions() {
        ((Activity)context).requestPermissions(NECESSARY_PERMISSIONS, 1);
    }
}