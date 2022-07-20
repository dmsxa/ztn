

package com.dmsxa.mobile.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import android.widget.Toast;

import com.dmsxa.maplib.api.ILayer;
import com.dmsxa.maplib.map.MapBase;
import com.dmsxa.maplib.map.MapContentProviderHelper;
import com.dmsxa.maplib.util.FileUtil;
import com.dmsxa.maplib.util.HttpResponse;
import com.dmsxa.maplib.util.NetworkUtil;
import com.dmsxa.maplib.util.SettingsConstants;
import com.dmsxa.maplibui.fragment.NGPreferenceSettingsFragment;
import com.dmsxa.maplibui.util.ControlHelper;
import com.dmsxa.maplibui.util.SettingsConstantsUI;
import com.dmsxa.mobile.MainApplication;
import com.dmsxa.mobile.R;
import com.dmsxa.mobile.activity.MainActivity;
import com.dmsxa.mobile.util.AppConstants;
import com.dmsxa.mobile.util.IntEditTextPreference;
import com.dmsxa.mobile.util.SelectMapPathPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static com.dmsxa.maplib.util.Constants.MAP_EXT;
import static com.dmsxa.maplib.util.SettingsConstants.KEY_PREF_MAP;
import static com.dmsxa.maplib.util.SettingsConstants.KEY_PREF_UNITS;
import static com.dmsxa.maplibui.service.TrackerService.HOST;
import static com.dmsxa.maplibui.service.TrackerService.URL;
import static com.dmsxa.maplibui.service.TrackerService.getUid;
import static com.dmsxa.maplibui.service.TrackerService.isTrackerServiceRunning;
import static com.dmsxa.mobile.util.AppSettingsConstants.KEY_PREF_GA;
import static com.dmsxa.mobile.util.AppSettingsConstants.KEY_PREF_SHOW_COMPASS;
import static com.dmsxa.mobile.util.AppSettingsConstants.KEY_PREF_SHOW_MEASURING;
import static com.dmsxa.mobile.util.AppSettingsConstants.KEY_PREF_SHOW_SCALE_RULER;
import static com.dmsxa.mobile.util.AppSettingsConstants.KEY_PREF_SHOW_ZOOM_CONTROLS;


public class SettingsFragment
        extends NGPreferenceSettingsFragment
        implements SelectMapPathPreference.OnAttachedListener
{
    @Override
    public  void createPreferences(PreferenceScreen screen)
    {
        switch (mAction) {
            case SettingsConstantsUI.ACTION_PREFS_GENERAL:
                addPreferencesFromResource(R.xml.preferences_general);

                final ListPreference theme =
                        (ListPreference) findPreference(SettingsConstantsUI.KEY_PREF_THEME);
                initializeTheme(getActivity(), theme);
                final Preference reset =
                        findPreference(SettingsConstantsUI.KEY_PREF_RESET_SETTINGS);
                initializeReset(getActivity(), reset);
                break;
            case SettingsConstantsUI.ACTION_PREFS_MAP:
                addPreferencesFromResource(R.xml.preferences_map);

                final ListPreference showInfoPanel = (ListPreference) findPreference(
                        SettingsConstantsUI.KEY_PREF_SHOW_STATUS_PANEL);
                initializeShowStatusPanel(showInfoPanel);

                final ListPreference lpCoordinateFormat =
                        (ListPreference) findPreference(SettingsConstantsUI.KEY_PREF_COORD_FORMAT);
                final IntEditTextPreference etCoordinateFraction =
                        (IntEditTextPreference) findPreference(
                                SettingsConstantsUI.KEY_PREF_COORD_FRACTION);
                initializeCoordinates(lpCoordinateFormat, etCoordinateFraction);

                final ListPreference lpUnits = (ListPreference) findPreference(KEY_PREF_UNITS);
                initializeUnits(lpUnits);

                final SelectMapPathPreference mapPath = (SelectMapPathPreference) findPreference(
                        SettingsConstants.KEY_PREF_MAP_PATH);
                mapPath.setOnAttachedListener(this);
                initializeMapPath(getActivity(), mapPath);

                final ListPreference showCurrentLocation = (ListPreference) findPreference(
                        SettingsConstantsUI.KEY_PREF_SHOW_CURRENT_LOC);
                initializeShowCurrentLocation(showCurrentLocation);

                final ListPreference changeMapBG =
                        (ListPreference) findPreference(SettingsConstantsUI.KEY_PREF_MAP_BG);
                initializeMapBG(changeMapBG);

                final Preference restore =
                        findPreference(SettingsConstantsUI.KEY_PREF_RESTORE_LAYERS);
                initializeRestore(getActivity(), restore);
                break;
            case SettingsConstantsUI.ACTION_PREFS_LOCATION:
                addPreferencesFromResource(R.xml.preferences_location);

                final ListPreference lpLocationAccuracy =
                        (ListPreference) findPreference(SettingsConstants.KEY_PREF_LOCATION_SOURCE);
                initializeLocationAccuracy(lpLocationAccuracy, false);

                final ListPreference minTimeLoc = (ListPreference) findPreference(
                        SettingsConstants.KEY_PREF_LOCATION_MIN_TIME);
                final ListPreference minDistanceLoc = (ListPreference) findPreference(
                        SettingsConstants.KEY_PREF_LOCATION_MIN_DISTANCE);
                initializeLocationMins(minTimeLoc, minDistanceLoc, false);

                final EditTextPreference accurateMaxCount = (EditTextPreference) findPreference(
                        SettingsConstants.KEY_PREF_LOCATION_ACCURATE_COUNT);
                initializeAccurateTaking(accurateMaxCount);
                break;
            case SettingsConstantsUI.ACTION_PREFS_TRACKING:
                addPreferencesFromResource(R.xml.preferences_tracks);

                final ListPreference lpTracksAccuracy =
                        (ListPreference) findPreference(SettingsConstants.KEY_PREF_TRACKS_SOURCE);
                initializeLocationAccuracy(lpTracksAccuracy, true);

                final ListPreference minTime =
                        (ListPreference) findPreference(SettingsConstants.KEY_PREF_TRACKS_MIN_TIME);
                final ListPreference minDistance = (ListPreference) findPreference(
                        SettingsConstants.KEY_PREF_TRACKS_MIN_DISTANCE);
                initializeLocationMins(minTime, minDistance, true);

                final CheckBoxPreference uid = (CheckBoxPreference) findPreference(SettingsConstants.KEY_PREF_TRACK_SEND);
                initializeUid(uid);
                break;
        }
    }


    protected static void restoreLayer(Activity activity, CharSequence name) {
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(activity);
        File defaultPath = activity.getExternalFilesDir(SettingsConstants.KEY_PREF_MAP);
        if (defaultPath == null) {
            defaultPath = new File(activity.getFilesDir(), SettingsConstants.KEY_PREF_MAP);
        }
        String mapPath = preferences.getString(SettingsConstants.KEY_PREF_MAP_PATH, defaultPath.getPath());
        String mapName = preferences.getString(SettingsConstantsUI.KEY_PREF_MAP_NAME, "default");
        File mapFullPath = new File(mapPath, mapName + MAP_EXT);
        try {
            String json = FileUtil.readFromFile(mapFullPath);
            JSONObject map = new JSONObject(json);
            JSONArray layers = map.optJSONArray("layers");
            if (layers != null) {
                JSONObject layer = new JSONObject();
                layer.put("path", name);
                layers.put(layer);
                map.put("layers", layers);
                FileUtil.writeToFile(mapFullPath, map.toString());
                Intent intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                Runtime.getRuntime().exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected static CharSequence[] fetchLayers(Activity activity)
    {
        ArrayList<String> items = new ArrayList<>();
        ArrayList<String> existing = new ArrayList<>();
        MainApplication app = (MainApplication) activity.getApplication();
        for (int i = app.getMap().getLayerCount() - 1; i >= 0; i--) {
            ILayer layer = app.getMap().getLayer(i);
            existing.add(layer.getPath().getName());
        }
        try {
            SQLiteDatabase db = ((MapContentProviderHelper) MapBase.getInstance()).getDatabase(false);
            Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

            if (c.moveToFirst()) {
                do {
                    String table = c.getString(0);
                    if (table.startsWith("track") || table.startsWith("sqlite") || table.startsWith("android") || existing.contains(table))
                        continue;
                    items.add(table);
                } while (c.moveToNext());
            }
            c.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return items.toArray(new CharSequence[items.size()]);
    }


    @Override
    public FragmentManager getFragmentManagerFromParentFragment()
    {
        return getFragmentManager();
    }


    public static void initializeTheme(
            final Activity activity,
            final ListPreference theme)
    {
        if (null != theme) {
            theme.setSummary(theme.getEntry());

            theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(
                        Preference preference,
                        Object newValue)
                {
                    activity.startActivity(activity.getIntent());
                    activity.finish();
                    return true;
                }
            });
        }
    }


    public static void initializeReset(
            final Activity activity,
            final Preference preference)
    {
        if (null != preference) {
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    AlertDialog.Builder confirm = new AlertDialog.Builder(activity);
                    confirm.setTitle(R.string.reset_settings_title)
                            .setMessage(R.string.reset_settings_message)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which)
                                        {
                                            resetSettings(activity);
                                            deleteLayers(activity);
                                            ((MainApplication) activity.getApplication()).initBaseLayers();
                                        }
                                    })
                            .show();
                    return false;
                }
            });
        }
    }


    public static void initializeRestore(
            final Activity activity,
            final Preference preference)
    {
        if (null != preference) {
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    final int[] selectedItem = {-1};
                    final CharSequence[] items = fetchLayers(activity);
                    AlertDialog.Builder confirm = new AlertDialog.Builder(activity);
                    confirm.setTitle(R.string.select_layer_to_restore)
                            .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    selectedItem[0] = which;
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which)
                                        {
                                            if (selectedItem[0] > -1)
                                                restoreLayer(activity, items[selectedItem[0]]);
                                        }
                                    })
                            .show();
                    return false;
                }
            });
        }
    }


    public static void initializeShowStatusPanel(final ListPreference listPreference)
    {
        listPreference.setSummary(listPreference.getEntry());

        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(
                    Preference preference,
                    Object newValue)
            {
                preference.setSummary(listPreference.getEntries()[listPreference.findIndexOfValue(
                        (String) newValue)]);

                return true;
            }
        });
    }


    public static void initializeUnits(final ListPreference lpUnits) {
        lpUnits.setSummary(lpUnits.getEntry());
        lpUnits.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean metric = newValue.equals("metric");
                        preference.setSummary(lpUnits.getEntries()[metric ? 0 : 1]);
                        return true;
                    }
                });
    }


    public static void initializeCoordinates(
            ListPreference lpCoordinateFormat,
            IntEditTextPreference etCoordinateFraction)
    {
        if (null != lpCoordinateFormat) {
            lpCoordinateFormat.setSummary(lpCoordinateFormat.getEntry());

            lpCoordinateFormat.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener()
                    {
                        @Override
                        public boolean onPreferenceChange(
                                Preference preference,
                                Object newValue)
                        {
                            int value = Integer.parseInt(newValue.toString());
                            CharSequence summary =
                                    ((ListPreference) preference).getEntries()[value];
                            preference.setSummary(summary);
                            return true;
                        }
                    });
        }

        if (etCoordinateFraction != null) {
            etCoordinateFraction.setSummary(etCoordinateFraction.getPersistedString(
                    "" + AppConstants.DEFAULT_COORDINATES_FRACTION_DIGITS));

            etCoordinateFraction.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener()
                    {
                        @Override
                        public boolean onPreferenceChange(
                                Preference preference,
                                Object newValue)
                        {
                            preference.setSummary(newValue.toString());
                            return true;
                        }
                    });
        }
    }


    public static void initializeMapPath(
            final Context context,
            final SelectMapPathPreference mapPath)
    {
        if (null != mapPath) {
            mapPath.setSummary(mapPath.getText());

            mapPath.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(
                        Preference preference,
                        Object o)
                {
                    final Activity parent = (Activity) context;
                    if (null == parent) {
                        return false;
                    }

                    File newPath = new File((String) o);
                    if (newPath.listFiles().length != 0) {
                        Toast.makeText(context,
                                context.getString(R.string.warning_folder_should_be_empty),
                                Toast.LENGTH_LONG).show();
                        return false;
                    }

                    moveMap(parent, newPath);

                    return true;
                }
            });
        }
    }


    public static void initializeShowCurrentLocation(
            final ListPreference listPreference)
    {
        listPreference.setSummary(listPreference.getEntry());

        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(
                    Preference preference,
                    Object newValue)
            {
                preference.setSummary(listPreference.getEntries()[listPreference.findIndexOfValue(
                        (String) newValue)]);

                return true;
            }
        });
    }


    public static void initializeMapBG(final ListPreference mapBG)
    {
        mapBG.setSummary(mapBG.getEntry());

        mapBG.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(
                    Preference preference,
                    Object newValue)
            {
                preference.setSummary(
                        mapBG.getEntries()[mapBG.findIndexOfValue((String) newValue)]);
                return true;
            }
        });
    }


    public static CharSequence[] getAccuracyEntries(Context context) {
        CharSequence[] entries = new CharSequence[3];
        entries[0] = context.getString(R.string.pref_location_accuracy_gps);
        entries[1] = context.getString(R.string.pref_location_accuracy_cell);
        entries[2] = entries[0] + " & " + entries[1];
        return entries;
    }

    public static void initializeLocationAccuracy(
            final ListPreference listPreference,
            final boolean isTracks)
    {
        if (listPreference != null) {
            CharSequence[] entries = getAccuracyEntries(listPreference.getContext());
            listPreference.setEntries(entries);
            listPreference.setSummary(listPreference.getEntry());

            listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(
                        Preference preference,
                        Object newValue)
                {
                    int value = Integer.parseInt(newValue.toString());
                    CharSequence summary = ((ListPreference) preference).getEntries()[value - 1];
                    preference.setSummary(summary);

                    sectionWork(preference.getContext(), isTracks);

                    return true;
                }
            });
        }
    }

    public static void initializeUid(CheckBoxPreference preference) {
        // async check
        // registered = true - enabled = true; keep state
        // registered = false - enabled = false; checked = false
        // no network - enabled = false; keep state; no network info
        Context context = preference.getContext();
        String uid = getUid(context);
        preference.setSummary(context.getString(R.string.track_uid, uid));
        new CheckRegistration(preference).execute();
    }

    private static class CheckRegistration extends AsyncTask<Void, Void, Boolean> {
        private CheckBoxPreference mPreference;

        CheckRegistration(CheckBoxPreference preference) {
            mPreference = preference;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String base = mPreference.getSharedPreferences().getString("tracker_hub_url", HOST);
                String url = String.format("%s/%s/registered", base + URL, getUid(mPreference.getContext()));
                HttpResponse response = NetworkUtil.get(url, null, null, false);
                String body = response.getResponseBody();
                JSONObject json = new JSONObject(body == null ? "" : body);
                return json.optBoolean("registered");
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result == null) {
                mPreference.setSummary(R.string.error_connect_failed);
                return;
            }

            Context context = mPreference.getContext();
            String uid = getUid(context);
            mPreference.setSummary(context.getString(R.string.track_uid, uid));

            if (result) {
                mPreference.setEnabled(true);
            } else {
                mPreference.setChecked(false);
            }
        }
    }

    public static void initializeLocationMins(
            ListPreference minTime,
            final ListPreference minDistance,
            final boolean isTracks)
    {
        final Context context = minDistance.getContext();
        minTime.setSummary(getMinSummary(context, minTime.getEntry(), minTime.getValue()));
        minDistance.setSummary(
                getMinSummary(context, minDistance.getEntry(), minDistance.getValue()));

        minTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(
                    Preference preference,
                    Object newValue)
            {
                int id = ((ListPreference) preference).findIndexOfValue((String) newValue);
                preference.setSummary(
                        getMinSummary(context, ((ListPreference) preference).getEntries()[id],
                                (String) newValue));

                String preferenceKey = isTracks
                                       ? SettingsConstants.KEY_PREF_TRACKS_MIN_TIME
                                       : SettingsConstants.KEY_PREF_LOCATION_MIN_TIME;
                preference.getSharedPreferences()
                        .edit()
                        .putString(preferenceKey, (String) newValue)
                        .apply();

                sectionWork(preference.getContext(), isTracks);

                return true;
            }
        });

        minDistance.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(
                    Preference preference,
                    Object newValue)
            {
                int id = ((ListPreference) preference).findIndexOfValue((String) newValue);
                preference.setSummary(
                        getMinSummary(context, ((ListPreference) preference).getEntries()[id],
                                (String) newValue));

                String preferenceKey = isTracks
                                       ? SettingsConstants.KEY_PREF_TRACKS_MIN_DISTANCE
                                       : SettingsConstants.KEY_PREF_LOCATION_MIN_DISTANCE;
                preference.getSharedPreferences()
                        .edit()
                        .putString(preferenceKey, (String) newValue)
                        .apply();

                sectionWork(preference.getContext(), isTracks);

                return true;
            }
        });
    }


    public static void initializeAccurateTaking(EditTextPreference accurateMaxCount)
    {
        accurateMaxCount.setSummary(accurateMaxCount.getText());

        accurateMaxCount.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(
                    Preference preference,
                    Object newValue)
            {
                preference.setSummary((CharSequence) newValue);
                return true;
            }
        });
    }


    private static String getMinSummary(
            Context context,
            CharSequence newEntry,
            String newValue)
    {
        int value = 0;

        try {
            value = Integer.parseInt(newValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        String addition = newEntry + "";
        addition += value == 0 ? context.getString(R.string.frequentest) : "";

        return addition;
    }


    protected static void sectionWork(
            Context context,
            boolean isTracks)
    {
        if (!isTracks) {
            MainApplication application = (MainApplication) context.getApplicationContext();
            application.getGpsEventSource().updateActiveListeners();
        } else {
            if (isTrackerServiceRunning(context)) {
                Toast.makeText(
                        context, context.getString(R.string.tracks_reload), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


    protected static void resetSettings(Activity activity)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(SettingsConstantsUI.KEY_PREF_THEME);
        editor.remove(SettingsConstantsUI.KEY_PREF_COMPASS_TRUE_NORTH);
        editor.remove(SettingsConstantsUI.KEY_PREF_COMPASS_MAGNETIC);
        editor.remove(SettingsConstantsUI.KEY_PREF_COMPASS_KEEP_SCREEN);
        editor.remove(SettingsConstantsUI.KEY_PREF_COMPASS_VIBRATE);
        editor.remove(SettingsConstantsUI.KEY_PREF_SHOW_STATUS_PANEL);
        editor.remove(SettingsConstantsUI.KEY_PREF_SHOW_CURRENT_LOC);
        editor.remove(KEY_PREF_SHOW_COMPASS);
        editor.remove(SettingsConstantsUI.KEY_PREF_KEEPSCREENON);
        editor.remove(SettingsConstantsUI.KEY_PREF_COORD_FORMAT);
        editor.remove(KEY_PREF_SHOW_ZOOM_CONTROLS);
        editor.remove(SettingsConstants.KEY_PREF_LOCATION_SOURCE);
        editor.remove(SettingsConstants.KEY_PREF_LOCATION_MIN_TIME);
        editor.remove(SettingsConstants.KEY_PREF_LOCATION_MIN_DISTANCE);
        editor.remove(SettingsConstants.KEY_PREF_LOCATION_ACCURATE_COUNT);
        editor.remove(SettingsConstants.KEY_PREF_TRACKS_SOURCE);
        editor.remove(SettingsConstants.KEY_PREF_TRACKS_MIN_TIME);
        editor.remove(SettingsConstants.KEY_PREF_TRACKS_MIN_DISTANCE);
        editor.remove(SettingsConstants.KEY_PREF_TRACK_RESTORE);
        editor.remove(SettingsConstants.KEY_PREF_TRACK_SEND);
        editor.remove(KEY_PREF_SHOW_MEASURING);
        editor.remove(KEY_PREF_SHOW_SCALE_RULER);
        editor.remove(SettingsConstantsUI.KEY_PREF_SHOW_GEO_DIALOG);
        editor.remove(KEY_PREF_GA);

        File defaultPath = activity.getExternalFilesDir(KEY_PREF_MAP);
        if (defaultPath == null) {
            defaultPath = new File(activity.getFilesDir(), KEY_PREF_MAP);
        }

        editor.putString(SettingsConstants.KEY_PREF_MAP_PATH, defaultPath.getPath());
        editor.apply();

        PreferenceManager.setDefaultValues(activity, R.xml.preferences_general, true);
        PreferenceManager.setDefaultValues(activity, R.xml.preferences_map, true);
        PreferenceManager.setDefaultValues(activity, R.xml.preferences_location, true);
        PreferenceManager.setDefaultValues(activity, R.xml.preferences_tracks, true);

        moveMap(activity, defaultPath);
    }


    protected static void deleteLayers(Activity activity)
    {
        MainApplication app = (MainApplication) activity.getApplication();
        for (int i = app.getMap().getLayerCount() - 1; i >= 0; i--) {
            ILayer layer = app.getMap().getLayer(i);
            if (!layer.getPath().getName().equals(MainApplication.LAYER_MAP_IMG_W) && !layer.getPath().getName().equals(MainApplication.LAYER_MAP_CIA_W) && !layer.getPath()
                    .getName()
                    .equals(MainApplication.LAYER_A) && !layer.getPath()
                    .getName()
                    .equals(MainApplication.LAYER_B) && !layer.getPath()
                    .getName()
                    .equals(MainApplication.LAYER_C) && !layer.getPath()
                    .getName()
                    .equals(MainApplication.LAYER_TRACKS)) {
                layer.delete();
            }
        }

        try {
            ((MapContentProviderHelper) MapBase.getInstance()).getDatabase(false).execSQL("VACUUM");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }


    public static void moveMap(
            Activity activity,
            File path)
    {
        MainApplication application = (MainApplication) activity.getApplication();
        if (null == application) {
            return;
        }

        ContentResolver.cancelSync(null, application.getAuthority());

        BackgroundMoveTask moveTask = new BackgroundMoveTask(activity, application.getMap(), path);
        moveTask.execute();
    }


    protected static class BackgroundMoveTask
            extends AsyncTask<Void, Void, Void>
    {
        private ProgressDialog mProgressDialog;
        private Activity       mActivity;
        private MapBase        mMap;
        private File           mPath;


        BackgroundMoveTask(
                Activity activity,
                MapBase map,
                File path)
        {
            mActivity = activity;
            mMap = map;
            mPath = path;
        }


        @Override
        protected Void doInBackground(Void... voids)
        {
            mMap.moveTo(mPath);
            return null;
        }


        @Override
        protected void onPreExecute()
        {
            //not good solution but rare used so let it be
            ControlHelper.lockScreenOrientation(mActivity);
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setTitle(R.string.moving);
            mProgressDialog.setMessage(mActivity.getString(R.string.warning_map_moving));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIcon(R.drawable.ic_action_warning_light);
            mProgressDialog.show();
        }


        @Override
        protected void onPostExecute(Void aVoid)
        {
            mProgressDialog.dismiss();
            ControlHelper.unlockScreenOrientation(mActivity);
        }
    }
}
