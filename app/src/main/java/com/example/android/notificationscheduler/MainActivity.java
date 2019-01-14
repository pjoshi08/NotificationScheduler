package com.example.android.notificationscheduler;

import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int JOB_ID = 0;
    private JobScheduler scheduler;
    private Switch deviceIdle, deviceCharging;
    private NotificationManager nm;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceIdle = findViewById(R.id.idleSwitch);
        deviceCharging = findViewById(R.id.chargingSwitch);
        seekBar = findViewById(R.id.seekbar);
        final TextView seekbarProgress = findViewById(R.id.seekbarProgress);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 0)
                    seekbarProgress.setText(progress + " s");
                else
                    seekbarProgress.setText(getString(R.string.not_set));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    public void scheduleJob(View view) {
        scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        RadioGroup networkOptions = findViewById(R.id.networkOptions);
        int selectedNetworkId = networkOptions.getCheckedRadioButtonId();
        int selectedNetworkOption = 0;

        switch (selectedNetworkId){
            case R.id.noNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
                break;

            case R.id.anyNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY;
                break;

            case R.id.wifiNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED;
                break;
        }

        ComponentName component = new ComponentName(getPackageName(),
                NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, component)
                .setRequiredNetworkType(selectedNetworkOption)
                .setRequiresDeviceIdle(deviceIdle.isChecked())
                .setRequiresCharging(deviceCharging.isChecked());

        int seekBarInteger = seekBar.getProgress();
        boolean seekBarSet = seekBarInteger > 0;

        if (seekBarSet)
            builder.setOverrideDeadline(seekBarInteger * 1000);

        boolean constraintSet = selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE ||
                deviceIdle.isChecked() || deviceCharging.isChecked() || seekBarSet;

        if (constraintSet) {
            JobInfo jobInfo = builder.build();
            scheduler.schedule(jobInfo);

            Toast.makeText(this, getString(R.string.job_scheduled), Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(this, getString(R.string.select_constraint), Toast.LENGTH_LONG).show();
    }

    public void cancelJobs(View view) {

        if (scheduler != null){
            scheduler.cancelAll();
            scheduler = null;

            nm.cancelAll();
            Toast.makeText(this, getString(R.string.jobs_cancelled), Toast.LENGTH_LONG).show();
        }
    }
}
