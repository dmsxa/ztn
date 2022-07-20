

package com.dmsxa.mobile.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.dmsxa.maplib.util.AccountUtil;
import com.dmsxa.maplibui.activity.NGIDSettingsActivity;
import com.dmsxa.maplibui.activity.NGPreferenceActivity;
import com.dmsxa.maplibui.activity.NGWSettingsActivity;
import com.dmsxa.maplibui.fragment.NGIDSettingsFragment;
import com.dmsxa.maplibui.fragment.NGPreferenceSettingsFragment;
import com.dmsxa.maplibui.fragment.NGPreferenceHeaderFragment;
import com.dmsxa.maplibui.fragment.NGWSettingsFragment;
import com.dmsxa.maplibui.util.ControlHelper;
import com.dmsxa.maplibui.util.SettingsConstantsUI;
import com.dmsxa.mobile.R;
import com.dmsxa.mobile.fragment.SettingsFragment;
import com.dmsxa.mobile.fragment.SettingsHeaderFragment;
import com.dmsxa.mobile.util.AppConstants;
import com.dmsxa.mobile.util.Logger;

/**
 * Application preference
 */
public class SettingsActivity
        extends NGPreferenceActivity
{
    @Override
    public  String getPreferenceHeaderFragmentTag()
    {
        return AppConstants.FRAGMENT_SETTINGS_HEADER_FRAGMENT;
    }


    @Override
    public  NGPreferenceHeaderFragment getNewPreferenceHeaderFragment()
    {
        return new SettingsHeaderFragment();
    }


    @Override
    public  String getPreferenceSettingsFragmentTag()
    {
        return AppConstants.FRAGMENT_SETTINGS_FRAGMENT;
    }


    @Override
    public NGPreferenceSettingsFragment getNewPreferenceSettingsFragment(String subScreenKey)
    {
        NGPreferenceSettingsFragment fragment;
        switch (subScreenKey) {
            default:
                fragment = new SettingsFragment();
                break;
            case SettingsConstantsUI.ACTION_PREFS_NGW:
                fragment = new NGWSettingsFragment();
                break;
            case SettingsConstantsUI.ACTION_PREFS_NGID:
                fragment = new NGIDSettingsFragment();
                break;
        }
        return fragment;
    }


    @Override
    public String getTitleString()
    {
        return getString(R.string.action_settings);
    }


    @Override
    public void setTitle(PreferenceScreen preferenceScreen)
    {
        switch (preferenceScreen.getKey()) {
            default:
                super.setTitle(preferenceScreen);
                return;
            case SettingsConstantsUI.ACTION_PREFS_NGW:
            case SettingsConstantsUI.ACTION_PREFS_NGID:
                break;
        }
    }


    @Override
    public  void onStartSubScreen(PreferenceScreen preferenceScreen)
    {
        Intent intent;
        switch (preferenceScreen.getKey()) {
            default:
                super.onStartSubScreen(preferenceScreen);
                return;
            case SettingsConstantsUI.ACTION_PREFS_NGW:
                if (!AccountUtil.isProUser(this)) {
                    ControlHelper.showProDialog(this);
                    return;
                }
                intent = new Intent(this, NGWSettingsActivity.class);
                break;
            case SettingsConstantsUI.ACTION_PREFS_NGID:
                intent = new Intent(this, NGIDSettingsActivity.class);
                break;
        }
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("save_log", false)) {
            Logger.initialize(this);
        }
    }
}
