package com.prod.intelligent7.engineautostart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by eric on 2015/6/4.
 */
public class StartServiceAtBoot extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent serviceLauncher = new Intent(context, ConnectDaemonService.class);
            context.startService(serviceLauncher);
            Log.v("DAEMON", "DAEMON Service loaded at start");
        }
    }
}
