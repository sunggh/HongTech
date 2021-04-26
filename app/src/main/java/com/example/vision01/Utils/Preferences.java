package com.example.vision01.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

//------------------------------------------------------------------------------------------------//
//
//------------------------------------------------------------------------------------------------//
public class Preferences
{

    public static final String DB_UPDATE_DATE           	= "DB_UPDATE_DATE";

    private static Context context = null;
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static void setContext(Context context)
    {
        Preferences.context = context;
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static void initial()
    {
        if( getString(DB_UPDATE_DATE) == null ) putString(DB_UPDATE_DATE, "");
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static void clear(Context context)
    {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit();

        prefsEditor.clear();
        prefsEditor.commit();
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static boolean getBoolean(String key)
    {
        return getBoolean(context, key);
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static boolean getBoolean(Context context, String key)
    {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(key, false);


    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static void putBoolean(String key, boolean value)
    {
        putBoolean(context, key, value);
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static void putBoolean(Context context, String key, boolean value)
    {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit();

        prefsEditor.putBoolean(key, value);
        prefsEditor.commit();
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static int getInt(String key)
    {
        return getInt(context, key);
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static int getInt(Context context, String key)
    {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(key, 0);
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static void putInt(String key, int value)
    {
        putInt(context, key, value);
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static void putInt(Context context, String key, int value)
    {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit();

        prefsEditor.putInt(key, value);
        prefsEditor.commit();
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static long getLong(String key)
    {
        return getLong(context, key);
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static long getLong(Context context, String key)
    {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(key, 0);
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static void putLong(String key, long value)
    {
        putLong(context, key, value);
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static void putLong(Context context, String key, long value)
    {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit();

        prefsEditor.putLong(key, value);
        prefsEditor.commit();
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static String getString(String key)
    {
        return getString(context, key);
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static String getString(Context context, String key)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, null);
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static void putString(String key, String value)
    {
        putString(context, key, value);
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static void putString(Context context, String key, String value)
    {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit();

        prefsEditor.putString(key, value);
        prefsEditor.commit();
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static void putArray(Context context, String key, ArrayList<String> values)
    {
        ArrayList<String> tmpArr = getArray(context, key);

        for( int index = 0; index < values.size(); index++)
            tmpArr.add(values.get(index));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++)
            a.put(values.get(i));

        if (!values.isEmpty())
            editor.putString(key, a.toString());
        else
            editor.putString(key, null);

        editor.apply();
    }
    //--------------------------------------------------------------------------------------------//
    //
    //--------------------------------------------------------------------------------------------//
    public static ArrayList<String> getArray(Context context, String key)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> strArr = new ArrayList<String>();


        if (json != null)
        {
            try
            {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++)
                {
                    String url = a.optString(i);
                    strArr.add(url);
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        return strArr;
    }

    public static void putDaysArray(Context context, String key, ArrayList<String> values)
    {
        ArrayList<String> tmpArr = getArray(context, key);

        for( int index = 0; index < values.size(); index++)
            tmpArr.add(values.get(index));


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();

        for( int index = 0; index < 7; index++ )
        {
            if( values.get(index) == "" || values.get(index) == null || values.get(index).length() == 0 )
                a.put("");
            else
                a.put(values.get(index));
        }

        for (int i = 0; i < values.size(); i++)
            a.put(values.get(i));

        if (!values.isEmpty())
            editor.putString(key, a.toString());
        else
            editor.putString(key, null);

        editor.apply();
    }
}