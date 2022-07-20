

package com.dmsxa.mobile.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.dmsxa.maplibui.fragment.CompassFragment;
import com.dmsxa.maplibui.util.SettingsConstantsUI;
import com.dmsxa.mobile.R;
import com.dmsxa.mobile.activity.MainActivity;
import com.nineoldandroids.view.ViewHelper;

public class FullCompassFragment extends CompassFragment {
    protected MainActivity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mActivity = (MainActivity) getActivity();
        mActivity.hideBottomBar();

        View view = inflater.inflate(R.layout.fragment_compass_full, container, false);
        FrameLayout compassContainer = (FrameLayout) view.findViewById(R.id.compass_container);
        compassContainer.addView(super.onCreateView(inflater, container, savedInstanceState));
        view.findViewById(R.id.action_add_point).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mIsVibrationOn = prefs.getBoolean(SettingsConstantsUI.KEY_PREF_COMPASS_VIBRATE, true);
        mParent.setKeepScreenOn(prefs.getBoolean(SettingsConstantsUI.KEY_PREF_COMPASS_KEEP_SCREEN, true));
        mTrueNorth = prefs.getBoolean(SettingsConstantsUI.KEY_PREF_COMPASS_TRUE_NORTH, true);
        mShowMagnetic = prefs.getBoolean(SettingsConstantsUI.KEY_PREF_COMPASS_MAGNETIC, true);

        mActivity.setActionBarState(false);
        mActivity.setTitle(R.string.compass_title);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getItemId() == R.id.menu_about || item.getItemId() == R.id.menu_settings) {
                continue;
            }
            item.setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDestroyView()
    {
        mActivity.restoreBottomBar(-1);
        mActivity.setTitle(mActivity.getAppName());
        super.onDestroyView();
    }


    @Override
    public void updateCompass(float azimuth) {
        float alpha = 1f;
        if (mShowMagnetic) {
            alpha = .3f;
        }

        ViewHelper.setAlpha(mCompassNeedleMagnetic, alpha);
        ViewHelper.setAlpha(mCompassNeedle, alpha);

        super.updateCompass(azimuth);
    }
}
