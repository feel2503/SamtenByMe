package kr.co.thiscat.samtenbyme;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceUtil
{
    private SharedPreferences mPref;
    private Context mContext;

    public static final String KEY_SHOW_URL1 = "Show URL 1";
    public static final String KEY_SHOW_URL2 = "Show URL 2";
    public static final String KEY_REVERSE = "Reverse View";

    public PreferenceUtil(Context context)
    {
        mContext = context;
        if(mPref == null)
            mPref = PreferenceManager.getDefaultSharedPreferences(context);

    }

    public void putStringPrefrence(String key, String value)
    {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getStringPreference(String key)
    {
        return mPref.getString(key, null);
    }

    public String getStringPreference(String key, String defaultValue)
    {
        return mPref.getString(key, defaultValue);
    }


    public void putBooleanPreference(String key, boolean value)
    {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBooleanPreference(String key)
    {
        return mPref.getBoolean(key, false);
    }

    public boolean getBooleanPreference(String key, boolean defaultValue)
    {
        return mPref.getBoolean(key, defaultValue);
    }

    public int getIntPreference(String key)
    {
        return getIntPreference(key, 0);
    }

    public int getIntPreference(String key, int defaultValue)
    {
        return mPref.getInt(key, defaultValue);
    }

    public void putIntPreference(String key, int value)
    {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public void putLongPreference(String key, long value)
    {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public long getLongPreference(String key)
    {
        return mPref.getLong(key, 0);
    }

    public void removePreference(String key)
    {
        mPref.edit().remove(key).commit();
    }

    public void clearData()
    {
        SharedPreferences.Editor editor = mPref.edit();
        editor.clear();
        editor.commit();
    }
}
