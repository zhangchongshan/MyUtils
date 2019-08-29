package com.example.myutils.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 偏好设置类
 */

public class MyPreferences extends Object {
    protected static MyPreferences instance = null;
    public synchronized static MyPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new MyPreferences(context);
        }
        return instance;
    }
    protected Context context;
    SharedPreferences thePrefs;
    protected MyPreferences(Context con) {
        if (context == null) {
            context = con;
            thePrefs = context.getSharedPreferences("com.sonoptek.smartuskit.preferences",context.MODE_PRIVATE);
        }
    }
    public String getString(String key, String defaultValue) {
        return thePrefs.getString(key,defaultValue);
    }
    public int getInt(String key, int defaultValue) {
        return thePrefs.getInt(key,defaultValue);
    }
    public boolean getBoolean(String key, boolean defaultValue) {
        return thePrefs.getBoolean(key,defaultValue);
    }
    public float getFloat(String key, float defaultValue) {
        return thePrefs.getFloat(key,defaultValue);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = thePrefs.edit();
        editor.putString(key,value);
        editor.commit();
    }
    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = thePrefs.edit();
        editor.putInt(key,value);
        editor.commit();
    }
    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = thePrefs.edit();
        editor.putBoolean(key,value);
        editor.commit();
    }
    public void putFloat(String key, float value) {
        SharedPreferences.Editor editor = thePrefs.edit();
        editor.putFloat(key,value);
        editor.commit();
    }

}
