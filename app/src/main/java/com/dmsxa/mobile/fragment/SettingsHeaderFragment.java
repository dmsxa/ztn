

package com.dmsxa.mobile.fragment;

import androidx.preference.PreferenceScreen;

import com.dmsxa.maplib.util.AccountUtil;
import com.dmsxa.maplibui.fragment.NGPreferenceHeaderFragment;
import com.dmsxa.maplibui.util.SettingsConstantsUI;
import com.dmsxa.mobile.R;


public class SettingsHeaderFragment
        extends NGPreferenceHeaderFragment
{
    @Override
    public  void createPreferences(PreferenceScreen screen)
    {
        addPreferencesFromResource(R.xml.preference_headers);
        if (getActivity() != null && !AccountUtil.isProUser(getActivity()))
            screen.findPreference(SettingsConstantsUI.ACTION_PREFS_NGW).setIcon(R.drawable.ic_lock_black_24dp);
    }
}
