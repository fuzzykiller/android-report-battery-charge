package in.futuretech.android.reportbatterycharge;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportChargeService extends Service {
    private final BroadcastReceiver batteryChargeBroadcastReceiver = new BatteryChargeBroadcastReceiver(this);
    private static final String logTag = ReportChargeService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i(logTag, "Service started");

        IntentFilter batteryChangedFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(batteryChargeBroadcastReceiver, batteryChangedFilter);

        reportBatteryState(batteryStatus);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // TODO: Can this identifier be saved in a location accessible from both manifest and here?
        Intent restartIntent = new Intent("in.futuretech.android.reportbatterycharge.RESTART_SERVICE");
        sendBroadcast(restartIntent);
        unregisterReceiver(batteryChargeBroadcastReceiver);

        Log.i(logTag, "Service destroyed");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void reportBatteryState(Intent intent) {
        ReportChargeTaskParams taskParams = new ReportChargeTaskParams(intent, getDeviceId());
        Log.i(logTag, String.format("Current charge level: %f (Charging: %b)", taskParams.ChargePercent, taskParams.IsCharging));

        new ReportChargeTask().execute(taskParams);
    }

    @SuppressLint("HardwareIds")
    private String getDeviceId() {
        // To identify the device when multiple devices are reporting to the same service
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private class BatteryChargeBroadcastReceiver extends BroadcastReceiver {
        private final ReportChargeService reportChargeService;

        public BatteryChargeBroadcastReceiver(ReportChargeService reportChargeService) {
            this.reportChargeService = reportChargeService;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            reportChargeService.reportBatteryState(intent);
        }
    }

    private final class ReportChargeTaskParams {
        ReportChargeTaskParams(Intent intent, String deviceId) {
            DeviceId = deviceId;
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            IsCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            ChargePercent = level / (float) scale;
        }

        final float ChargePercent;
        final boolean IsCharging;
        final String DeviceId;
    }

    private class ReportChargeTask extends AsyncTask<ReportChargeTaskParams, Void, Void> {

        @Override
        protected Void doInBackground(ReportChargeTaskParams... params) {
            ReportChargeTaskParams actualParams = params[0];

            try {
                URL url = new URL(
                        String.format((Locale)null,
                                "http://192.168.2.5/report-charge/report.php?device=%s&charge=%f&charging=%b",
                                actualParams.DeviceId,
                                actualParams.ChargePercent,
                                actualParams.IsCharging));

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    int responseCode = connection.getResponseCode();
                    if (responseCode < 200) {
                        Log.w(logTag, String.format("Report service unexpected response code: %d", responseCode));
                    } else if (responseCode < 400) {
                        Log.i(logTag, String.format("Report service response code: %d", responseCode));
                    } else {
                        Log.w(logTag, String.format("Report service error code: %d", responseCode));
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (MalformedURLException e) {
                Log.w(logTag, "Report service call failed: Invalid URL");
            } catch (IOException e) {
                Log.w(logTag, "Report service call failed: I/O error");
            }

            return null;
        }
    }
}
