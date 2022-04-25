package com.sandroid.ffmpegjava;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;


import java.io.File;
import java.io.IOException;

public class FFmpegService extends Service {
    FFmpeg fFmpeg;
    int duration;
    public MutableLiveData<Integer> percentage;
    IBinder myBinder = new LocalBinder();

    String[] command;
    Callback activity;

    @Override
    public void onStart(Intent intent, int startId) {
         super.onStart(intent, startId);
    }

    public FFmpegService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            duration = Integer.parseInt(intent.getStringExtra("duration"));
            command = intent.getStringArrayExtra("command");
            try {
                loadFFMpegBinary();
                executeFFMpegCommand();
            } catch (FFmpegNotSupportedException | FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            loadFFMpegBinary();
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
        percentage = new MutableLiveData<>();
    }

    private void executeFFMpegCommand() throws FFmpegCommandAlreadyRunningException {
        fFmpeg.execute(command,new ExecuteBinaryResponseHandler(){
            @Override
            public void onFailure(String message) {
                super.onFailure(message);
            }

            @Override
            public void onSuccess(String message) {
                super.onSuccess(message);
            }

            @Override
            public void onProgress(String message) {
            String arr[];
            if (message.contains("time=")){
                arr=message.split("time=");
                String yalo=arr[1];

                String abikamha[]=yalo.split(":");
                String[]yaenda=abikamha[2].split(" ");
                String second= yaenda[0];

                int hour=Integer.parseInt(abikamha[0]);
                hour=hour*3600;
                int min= Integer.parseInt(abikamha[1]);
                min=min*60;                                                                                                                                                                                           min=min*60;
                float sec = Float.valueOf(second);

                float timeInSec= hour+min+sec;

                percentage.setValue((int)((timeInSec/duration)*100));
            }
            }

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish() {
                percentage.setValue(100);
            }
        });
    }

    private void loadFFMpegBinary() throws FFmpegNotSupportedException {
        if (fFmpeg == null) {
            fFmpeg = FFmpeg.getInstance(this);
        }
        fFmpeg.loadBinary(new LoadBinaryResponseHandler() {
            @Override
            public void onFailure() {
                super.onFailure();
            }

            @Override
            public void onSuccess() {
                super.onSuccess();
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class LocalBinder extends Binder {
        public FFmpegService getServiceInstance() {
            return FFmpegService.this;
        }
    }

    public void registerClient(Activity activity) {
        this.activity = (Callback) activity;

    }

    public MutableLiveData<Integer> getPercentage() {
        return percentage;
    }

    public interface Callback {
        void updateClient(float data);
    }
}
