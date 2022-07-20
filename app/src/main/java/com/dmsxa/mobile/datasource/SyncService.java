
package com.dmsxa.mobile.datasource;

import android.content.Context;

import com.dmsxa.maplib.service.NGWSyncService;

public class SyncService extends NGWSyncService {
    @Override
    public SyncAdapter createSyncAdapter(Context context, boolean autoInitialize) {
        return new SyncAdapter(context, autoInitialize);
    }
}
