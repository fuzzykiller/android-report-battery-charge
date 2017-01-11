package in.futuretech.android.reportbatterycharge;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String deviceId = getDeviceId();

        setContentView(R.layout.activity_main);

        TextView myTextView = (TextView) findViewById(R.id.myTextView);
        myTextView.setText(String.format(getString(R.string.DeviceIDLabel), deviceId));

        Button copyButton = (Button) findViewById(R.id.copyButton);
        final MainActivity that = this;

        // Provide easy access to device ID as used in service calls
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("device id", deviceId);
                clipboard.setPrimaryClip(clip);

                Toast toast = Toast.makeText(that, R.string.TextCopied, Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        // Start service if it isn’t running
        if (!isMyServiceRunning(ReportChargeService.class)) {
            startService(new Intent(this, ReportChargeService.class));
        }
    }

    @SuppressLint("HardwareIds")
    private String getDeviceId() {
        // To identify the device when multiple devices are reporting to the same service
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    protected void onDestroy() {
        // Stop service. It’ll start again automatically, disassociated from the current “context” (or whatever)
        stopService(new Intent(this, ReportChargeService.class));

        super.onDestroy();
    }

    // Taken from http://fabcirablog.weebly.com/blog/creating-a-never-ending-background-service-in-android
    // License: MIT
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                Log.i("isMyServiceRunning?", String.format("%s is running", serviceClass.getSimpleName()));
                return true;
            }
        }

        Log.i("isMyServiceRunning?", String.format("%s is NOT running", serviceClass.getSimpleName()));
        return false;
    }
}
