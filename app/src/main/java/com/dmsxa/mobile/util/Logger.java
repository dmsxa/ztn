

package com.dmsxa.mobile.util;

import android.content.Context;
import android.util.Log;

import com.hypertrack.hyperlog.HyperLog;

public final class Logger {
    public static void initialize(Context context) {
        HyperLog.initialize(context);
        HyperLog.setLogLevel(Log.VERBOSE);
        HyperLog.setLogFormat(new CustomLogMessageFormat(context));
    }
}
