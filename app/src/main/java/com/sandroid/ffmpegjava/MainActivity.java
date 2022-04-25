package com.sandroid.ffmpegjava;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

public class MainActivity extends AppCompatActivity {

    FFmpeg fFmpeg;
    Uri selectedUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       /* try {
            loadFfmpegLibrary();
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }*/
    }

    public void loadFfmpegLibrary() throws FFmpegNotSupportedException {
        if (fFmpeg==null){
            fFmpeg=FFmpeg.getInstance(this);
            fFmpeg.loadBinary(new LoadBinaryResponseHandler(){
                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onFailure() {
                    super.onFailure();
                    Toast.makeText(MainActivity.this, "Library failed to load", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess() {
                    super.onSuccess();
                    Toast.makeText(MainActivity.this, "Library load successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                }
            });
        }
    }

    public void executeCommand(final String [] commands) throws FFmpegCommandAlreadyRunningException {
      fFmpeg.execute(commands,new ExecuteBinaryResponseHandler(){
          @Override
          public void onFailure(String message) {
              super.onFailure(message);
          }

          @Override
          public void onSuccess(String message) {
              super.onSuccess(message);
          }

          @Override
          public void onStart() {
              super.onStart();
          }


          @Override
          public void onProgress(String message) {
              super.onProgress(message);
          }

          @Override
          public void onFinish() {
              super.onFinish();
          }
      });
    }

    public void opneGalary(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        startActivityForResult(intent,101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==101&& resultCode==RESULT_OK){
            selectedUri=data.getData();
            Intent i=new Intent(MainActivity.this,TrimActivity.class);
            i.putExtra("uri",selectedUri.toString());
            startActivity(i);
        }
    }
}