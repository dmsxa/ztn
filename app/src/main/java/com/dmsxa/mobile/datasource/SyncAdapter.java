

package com.dmsxa.mobile.datasource;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;

import com.dmsxa.maplibui.util.NotificationHelper;
import com.dmsxa.mobile.R;
import com.dmsxa.mobile.activity.MainActivity;
import com.dmsxa.mobile.util.AppSettingsConstants;

import static com.dmsxa.maplibui.util.NotificationHelper.createBuilder;

public class SyncAdapter extends com.dmsxa.maplib.datasource.ngw.SyncAdapter {
    private static final int NOTIFICATION_ID = 517;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String authority, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        sendNotification(getContext(), SYNC_START, null);

        super.onPerformSync(account, bundle, authority, contentProviderClient, syncResult);

        if (isCanceled())
            sendNotification(getContext(), SYNC_CANCELED, null);
        else if (syncResult.hasError())
            sendNotification(getContext(), SYNC_CHANGES, mError);
        else
            sendNotification(getContext(), SYNC_FINISH, null);
    }

    public void sendNotification(
            Context context,
            String notificationType,
            String message)
    {
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(AppSettingsConstants.KEY_PREF_SHOW_SYNC, false))
            return;

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = createBuilder(context, com.dmsxa.maplibui.R.string.sync);
        builder.setSmallIcon(R.drawable.ic_action_sync)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOngoing(false);

        Bitmap largeIcon = NotificationHelper.getLargeIcon(R.drawable.ic_action_sync, context.getResources());
        switch (notificationType) {
            case SYNC_START:
                largeIcon = NotificationHelper.getLargeIcon(R.drawable.ic_next_dark, context.getResources());
                builder.setProgress(0, 0, true)
                        .setTicker(context.getString(com.dmsxa.maplib.R.string.sync_started))
                        .setContentTitle(context.getString(com.dmsxa.maplib.R.string.synchronization))
                        .setContentText(context.getString(com.dmsxa.maplib.R.string.sync_progress));
                break;

            case SYNC_FINISH:
                largeIcon = NotificationHelper.getLargeIcon(R.drawable.ic_action_apply_dark, context.getResources());
                builder.setProgress(0, 0, false)
                        .setTicker(context.getString(com.dmsxa.maplib.R.string.sync_finished))
                        .setContentTitle(context.getString(com.dmsxa.maplib.R.string.synchronization))
                        .setContentText(context.getString(com.dmsxa.maplib.R.string.sync_finished));
                break;

            case SYNC_CANCELED:
                largeIcon = NotificationHelper.getLargeIcon(R.drawable.ic_action_cancel_dark, context.getResources());
                builder.setProgress(0, 0, false)
                        .setTicker(context.getString(com.dmsxa.maplib.R.string.sync_canceled))
                        .setContentTitle(context.getString(com.dmsxa.maplib.R.string.synchronization))
                        .setContentText(context.getString(com.dmsxa.maplib.R.string.sync_canceled));
                break;

            case SYNC_CHANGES:
                largeIcon = NotificationHelper.getLargeIcon(R.drawable.ic_action_warning_dark, context.getResources());
                builder.setProgress(0, 0, false)
                        .setTicker(context.getString(com.dmsxa.maplib.R.string.sync_error))
                        .setContentTitle(context.getString(com.dmsxa.maplib.R.string.synchronization))
                        .setContentText(message);
                break;
        }

        builder.setLargeIcon(largeIcon);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
