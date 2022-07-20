
package com.dmsxa.mobile.util;

public interface AppSettingsConstants
{
    String AUTHORITY             = "com.dmsxa.mobile.provider";

    /**
     * Preference key - not UI
     */
    String KEY_PREF_SHOW_LOCATION = "map_show_loc";
    String KEY_PREF_SHOW_COMPASS  = "map_show_compass";
    String KEY_PREF_SHOW_INFO     = "map_show_info";
    String KEY_PREF_APP_VERSION   = "app_version";
    String KEY_PREF_SHOW_SYNC     = "show_sync";

    /**
     * Preference keys - in UI
     */
    String KEY_PREF_STORAGE_SITE        = "storage_site";
    String KEY_PREF_USER_ID             = "user_id";
    String KEY_PREF_MIN_DIST_CHNG_UPD   = "min_dist_change_for_update";
    String KEY_PREF_MIN_TIME_UPD        = "min_time_beetwen_updates";
    String KEY_PREF_SW_TRACK_SRV        = "sw_track_service";
    String KEY_PREF_SW_TRACKGPX_SRV     = "sw_trackgpx_service";
    String KEY_PREF_SHOW_LAYES_LIST     = "show_layers_list";
    String KEY_PREF_SW_SENDPOS_SRV      = "sw_sendpos_service";
    String KEY_PREF_SW_ENERGY_ECO       = "sw_energy_economy";
    String KEY_PREF_TIME_DATASEND       = "time_between_datasend";
    String KEY_PREF_ACCURATE_LOC        = "accurate_coordinates_pick";
    String KEY_PREF_ACCURATE_GPSCOUNT   = "accurate_coordinates_pick_count";
    String KEY_PREF_ACCURATE_CE         = "accurate_type";
    String KEY_PREF_TILE_SIZE           = "map_tile_size";
    String KEY_PREF_COMPASS_VIBRO       = "compass_vibration";
    String KEY_PREF_COMPASS_TRUE_NORTH  = "compass_true_north";
    String KEY_PREF_COMPASS_SHOW_MAGNET = "compass_show_magnetic";
    String KEY_PREF_COMPASS_WAKE_LOCK   = "compass_wake_lock";
    String KEY_PREF_SHOW_ZOOM_CONTROLS  = "show_zoom_controls";
    String KEY_PREF_SHOW_SCALE_RULER    = "show_scale_ruler";
    String KEY_PREF_SHOW_MEASURING      = "show_ruler_measuring";
    String KEY_PREF_GA                  = "ga_enabled";
    String KEY_PREF_INTRO               = "app_intro";
    String KEY_PREF_SHOW_ZOOM           = "show_zoom_level";

    int FIRSTSTART_DOWNLOADZOOM = 5;

    String GA_SCREEN_ABOUT = "About Screen";
    String GA_SCREEN_SUPPORT = "Support Screen";

    String APK_VERSION_UPDATE = "https://my.usermap.cn/downloads/software/mobile/version";
}
