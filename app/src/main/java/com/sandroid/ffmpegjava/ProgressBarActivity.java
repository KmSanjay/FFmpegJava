package com.sandroid.ffmpegjava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class ProgressBarActivity extends AppCompatActivity {

    ProgressBar circularProgressBar;
    int duration;
    String[] command;
    String path;
    File dest;

    ServiceConnection mConnection;
    FFmpegService fFmpegService;
    Integer res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_bar);

        circularProgressBar = findViewById(R.id.simpleProgressBar);
        circularProgressBar.setMax(100);

         Intent intent = getIntent();
        if (intent != null) {
            duration = intent.getIntExtra("duration", 0);
            command = intent.getStringArrayExtra("command");
            path = intent.getStringExtra("destination");

            Intent intent1 = new Intent(ProgressBarActivity.this, FFmpegService.class);
            intent1.putExtra("duration", String.valueOf(duration));
            intent1.putExtra("command", command);
            intent1.putExtra("destination", path);
            startService(intent1);

            mConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    FFmpegService.LocalBinder binder = (FFmpegService.LocalBinder) iBinder;
                    fFmpegService = binder.getServiceInstance();
                    fFmpegService.registerClient(getParent());

                    final Observer<Integer> resultObserver = new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            res = integer;
                            if (res < 100) {
                                circularProgressBar.setProgress(res);
                                circularProgressBar.setVisibility(View.VISIBLE);
                            }
                            if (res == 100) {
                                circularProgressBar.setProgress(res);
                                stopService(intent1);
                                circularProgressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Video trimmed successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                    fFmpegService.getPercentage().observe(ProgressBarActivity.this, resultObserver);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {

                }
            };
            bindService(intent1, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

}