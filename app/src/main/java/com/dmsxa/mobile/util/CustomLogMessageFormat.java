

package com.dmsxa.mobile.util;

import android.content.Context;

import com.hypertrack.hyperlog.LogFormat;
import com.dmsxa.mobile.BuildConfig;

public class CustomLogMessageFormat extends LogFormat {
    public CustomLogMessageFormat(Context context) {
        super(context);
    }

    @Override
    public String getFormattedLogMessage(String logLevelName, String tag, String message, String timeStamp, String senderName, String osVersion,
                                         String deviceUUID) {
        String uuid = deviceUUID;
        if (uuid == null) {
            uuid = "DeviceUUID";
        }
        String appTag = BuildConfig.VERSION_NAME;
        return timeStamp + " | " + senderName + " : " + osVersion + " | " + uuid + " | [" + logLevelName + "/" + appTag + "]: " + message;
    }
}
