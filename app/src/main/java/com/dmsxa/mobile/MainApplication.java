

package com.dmsxa.mobile;

import static com.dmsxa.maplib.util.Constants.MAP_EXT;
import static com.dmsxa.maplib.util.GeoConstants.TMSTYPE_OSM;
import static com.dmsxa.maplibui.fragment.NGWSettingsFragment.setAccountSyncEnabled;
import static com.dmsxa.mobile.util.AppSettingsConstants.AUTHORITY;
import static com.dmsxa.mobile.util.AppSettingsConstants.KEY_PREF_APP_VERSION;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.dmsxa.maplib.api.ILayer;
import com.dmsxa.maplib.datasource.Field;
import com.dmsxa.maplib.map.LayerGroup;
import com.dmsxa.maplib.map.MapBase;
import com.dmsxa.maplib.map.MapDrawable;
import com.dmsxa.maplib.map.NGWVectorLayer;
import com.dmsxa.maplib.map.VectorLayer;
import com.dmsxa.maplib.util.AccountUtil;
import com.dmsxa.maplib.util.Constants;
import com.dmsxa.maplib.util.GeoConstants;
import com.dmsxa.maplib.util.NGWUtil;
import com.dmsxa.maplib.util.SettingsConstants;
import com.dmsxa.maplibui.GISApplication;
import com.dmsxa.maplibui.mapui.LayerFactoryUI;
import com.dmsxa.maplibui.mapui.RemoteTMSLayerUI;
import com.dmsxa.maplibui.mapui.TrackLayerUI;
import com.dmsxa.maplibui.mapui.VectorLayerUI;
import com.dmsxa.maplibui.service.TrackerService;
import com.dmsxa.maplibui.util.SettingsConstantsUI;
import com.dmsxa.mobile.activity.SettingsActivity;
import com.dmsxa.mobile.util.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application class
 * The initial layers create here. Also upgrade db from previous version is here too.
 */
public class MainApplication extends GISApplication {
    public static final String KEY = "23480482370de5f27415a04f103a";
    public static final String LAYER_MAP_IMG_W = "tian_di_tu_img_w";
    public static final String LAYER_MAP_CIA_W = "tian_di_tu_cva_w";

    public static final String LAYER_OSM = "osm";
    public static final String LAYER_A = "vector_a";
    public static final String LAYER_B = "vector_b";
    public static final String LAYER_C = "vector_c";
    public static final String LAYER_TRACKS = "tracks";

    @Override
    public void onCreate() {
        //if (!BuildConfig.DEBUG)
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (mSharedPreferences.getBoolean("save_log", false)) {
            Logger.initialize(this);
        }

        super.onCreate();
        updateFromOldVersion();
        NGWUtil.NGUA = "ng_mobile";
        NGWUtil.UUID = TrackerService.getUid(this);
    }

    @Override
    public void sendScreen(String name) {
    }

    @Override
    public String getAccountsType() {
        return Constants.NGW_ACCOUNT_TYPE;
    }

    @Override
    public void sendEvent(String category, String action, String label) {
    }

    private void updateFromOldVersion() {
        try {
            int currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            int savedVersionCode = mSharedPreferences.getInt(KEY_PREF_APP_VERSION, 0);

            switch (savedVersionCode) {
                case 0:
                    int source;
                    if (mSharedPreferences.contains(SettingsConstants.KEY_PREF_LOCATION_SOURCE)) {
                        try {
                            source = mSharedPreferences.getInt(SettingsConstants.KEY_PREF_LOCATION_SOURCE, 3);
                        } catch (ClassCastException e) {
                            source = 3;
                        }
                        mSharedPreferences.edit()
                                .remove(SettingsConstants.KEY_PREF_LOCATION_SOURCE)
                                .remove(SettingsConstants.KEY_PREF_LOCATION_SOURCE + "_str")
                                .putString(SettingsConstants.KEY_PREF_LOCATION_SOURCE, source + "").apply();
                    }
                    if (mSharedPreferences.contains(SettingsConstants.KEY_PREF_TRACKS_SOURCE)) {
                        try {
                            source = mSharedPreferences.getInt(SettingsConstants.KEY_PREF_TRACKS_SOURCE, 1);
                        } catch (ClassCastException e) {
                            source = 3;
                        }
                        mSharedPreferences.edit()
                                .remove(SettingsConstants.KEY_PREF_TRACKS_SOURCE)
                                .remove(SettingsConstants.KEY_PREF_TRACKS_SOURCE + "_str")
                                .putString(SettingsConstants.KEY_PREF_TRACKS_SOURCE, source + "").apply();
                    }
                case 13:
                case 14:
                case 15:
                    mSharedPreferences.edit().remove(SettingsConstantsUI.KEY_PREF_SHOW_STATUS_PANEL)
                            .remove(SettingsConstantsUI.KEY_PREF_COORD_FORMAT + "_int")
                            .remove(SettingsConstantsUI.KEY_PREF_COORD_FORMAT).apply();
                default:
                    break;
            }

            if (savedVersionCode < 44) {
                if (!AccountUtil.isProUser(this)) {
                    if (isAccountManagerValid())
                        for (final Account account : mAccountManager.getAccountsByType(getAccountsType()))
                            setAccountSyncEnabled(account, getAuthority(), false);

                    for (int i = 0; i < mMap.getLayerCount(); i++) {
                        ILayer layer = mMap.getLayer(i);
                        if (layer instanceof NGWVectorLayer) {
                            NGWVectorLayer ngwLayer = (NGWVectorLayer) layer;
                            ngwLayer.setSyncType(Constants.SYNC_NONE);
                            ngwLayer.save();
                        }
                    }
                }
            }

            if (savedVersionCode < currentVersionCode) {
                mSharedPreferences.edit().putInt(KEY_PREF_APP_VERSION, currentVersionCode).apply();
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public MapBase getMap() {
        if (null != mMap) {
            return mMap;
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        File defaultPath = getExternalFilesDir(SettingsConstants.KEY_PREF_MAP);
        if (defaultPath == null) {
            defaultPath = new File(getFilesDir(), SettingsConstants.KEY_PREF_MAP);
        }

        String mapPath = mSharedPreferences.getString(SettingsConstants.KEY_PREF_MAP_PATH, defaultPath.getPath());
        String mapName = mSharedPreferences.getString(SettingsConstantsUI.KEY_PREF_MAP_NAME, "default");

        File mapFullPath = new File(mapPath, mapName + MAP_EXT);

        final Bitmap bkBitmap = getMapBackground();
        mMap = new MapDrawable(bkBitmap, this, mapFullPath, new LayerFactoryUI());
        mMap.setName(mapName);
        mMap.load();

        checkTracksLayerExist();

        return mMap;
    }


    protected void checkTracksLayerExist() {
        List<ILayer> tracks = new ArrayList<>();
        LayerGroup.getLayersByType(mMap, Constants.LAYERTYPE_TRACKS, tracks);
        if (tracks.isEmpty()) {
            String trackLayerName = getString(R.string.tracks);
            TrackLayerUI trackLayer =
                    new TrackLayerUI(getApplicationContext(), mMap.createLayerStorage(LAYER_TRACKS));
            trackLayer.setName(trackLayerName);
            trackLayer.setVisible(true);
            mMap.addLayer(trackLayer);
            mMap.save();
        }
    }


    @Override
    public String getAuthority() {
        return AUTHORITY;
    }

    @Override
    public void showSettings(String settings) {
        if (TextUtils.isEmpty(settings)) {
            settings = SettingsConstantsUI.ACTION_PREFS_GENERAL;
        }

        switch (settings) {
            case SettingsConstantsUI.ACTION_PREFS_GENERAL:
            case SettingsConstantsUI.ACTION_PREFS_LOCATION:
            case SettingsConstantsUI.ACTION_PREFS_TRACKING:
                break;
            default:
                return;
        }

        Intent intent = new Intent(this, SettingsActivity.class);
        intent.setAction(settings);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onFirstRun() {
        initBaseLayers();
    }


    public void initBaseLayers() {
        String layerName = this.getString(R.string.map_name_img_name);
        String layerURL = this.getString(R.string.map_img, KEY);
        initBaseLayers(LAYER_MAP_IMG_W, layerName, layerURL, TMSTYPE_OSM);

        layerName = getString(R.string.map_name_cia_name);
        layerURL = getString(R.string.map_cia, KEY);
        initBaseLayers(LAYER_MAP_CIA_W, layerName, layerURL, TMSTYPE_OSM);

        // create empty layers for first experimental editing
        List<Field> fields = new ArrayList<>(2);
        fields.add(new Field(GeoConstants.FTInteger, "FID", "FID"));
        fields.add(new Field(GeoConstants.FTString, "TEXT", "TEXT"));

        if (mMap.getLayerByPathName(LAYER_A) == null)
            mMap.addLayer(createEmptyVectorLayer(getString(R.string.points_for_edit), LAYER_A, GeoConstants.GTPoint, fields));
        if (mMap.getLayerByPathName(LAYER_B) == null)
            mMap.addLayer(createEmptyVectorLayer(getString(R.string.lines_for_edit), LAYER_B, GeoConstants.GTLineString, fields));
        if (mMap.getLayerByPathName(LAYER_C) == null)
            mMap.addLayer(createEmptyVectorLayer(getString(R.string.polygons_for_edit), LAYER_C, GeoConstants.GTPolygon, fields));

        mMap.save();
    }

    private void initBaseLayers(String id, String layerName, String layerURL, int type) {
        if (getMap().getLayerByPathName(id) == null) {
            final RemoteTMSLayerUI layer = new RemoteTMSLayerUI(this, mMap.createLayerStorage(id));
            layer.setName(layerName);
            layer.setURL(layerURL);
            layer.setTMSType(type);
            layer.setVisible(true);
            layer.setMinZoom(GeoConstants.DEFAULT_MIN_ZOOM);
            layer.setMaxZoom(19);

            //  mMap.addLayer(layer);
            mMap.addLayer(layer);

            new Handler().post(() -> {
                try {
                    layer.fillFromZip(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.maps), null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }


    public VectorLayer createEmptyVectorLayer(
            String layerName,
            String layerPath,
            int layerType,
            List<Field> fields) {
        VectorLayerUI vectorLayer = new VectorLayerUI(this, mMap.createLayerStorage(layerPath));
        vectorLayer.setName(layerName);
        vectorLayer.setVisible(true);
        vectorLayer.setMinZoom(GeoConstants.DEFAULT_MIN_ZOOM);
        vectorLayer.setMaxZoom(GeoConstants.DEFAULT_MAX_ZOOM);

        vectorLayer.create(layerType, fields);
        return vectorLayer;
    }
}
