package org.twinone.locker.version;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;


/**
 * Receives boot and alarm events for {@link VersionManager}
 *
 * @author twinone
 */
public class Receiver extends BroadcastReceiver {

    private static final String ACTION_QUERY_SERVER = "com.twinone.locker.version.intent.action.query_server";

    private static void scheduleAlarm(Context c) {
        Intent i = new Intent(c, Receiver.class);
        i.setAction(ACTION_QUERY_SERVER);

        PendingIntent pi = PendingIntent.getBroadcast(c, 0, i, 0);

        // schedule an alarm for in 5 seconds, that repeats every 12 hours
        AlarmManager am = (AlarmManager) c
                .getSystemService(Context.ALARM_SERVICE);
        long startTime = SystemClock.elapsedRealtime();
        long interval = AlarmManager.INTERVAL_HALF_DAY;
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startTime,
                interval, pi);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_QUERY_SERVER.equals(intent.getAction())) {
            onAlarmReceived(context);
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            scheduleAlarm(context);
        }
    }

    private void onAlarmReceived(final Context c) {
    }

}
