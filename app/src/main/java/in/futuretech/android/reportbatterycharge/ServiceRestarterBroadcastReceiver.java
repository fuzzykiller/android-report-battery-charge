package in.futuretech.android.reportbatterycharge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceRestarterBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("in.futuretech.android.reportbatterycharge.RESTART_SERVICE")) {
            Log.i(ServiceRestarterBroadcastReceiver.class.getSimpleName(), "Service stopping, restarting...");
        } else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.i(ServiceRestarterBroadcastReceiver.class.getSimpleName(), "Strarting service at boot...");
        }

        context.startService(new Intent(context, ReportChargeService.class));
    }
}
