package com.sandroid.ffmpegjava;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

import VideoHandle.EpEditor;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;

public class MainActivity extends AppCompatActivity {

    Uri selectedUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openGallery(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            selectedUri = data.getData();
            Intent i = new Intent(MainActivity.this, TrimActivity.class);
            i.putExtra("uri", selectedUri.toString());
            startActivity(i);
        } else if (requestCode == 102 && resultCode == RESULT_OK) {
            assert data != null;
            mergeSelectedVideos(data.getClipData());
        }
    }

    private void mergeSelectedVideos(ClipData clipData) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Merging videos");
        progressDialog.show();
        progressDialog.setCancelable(false);

        ArrayList<EpVideo> epVideos = new ArrayList<>();

        for (int i = 0; i < clipData.getItemCount(); i++) {
            epVideos.add(new EpVideo(getRealPathFromUri(this, clipData.getItemAt(i).getUri())));
            Log.d("aman", "" + getRealPathFromUri(this, clipData.getItemAt(i).getUri()));
        }

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "VideoEdit/Merge");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        EpEditor.OutputOption outputOption = new EpEditor.OutputOption(new File(folder, System.currentTimeMillis() + ".mp4").getAbsolutePath());
        outputOption.setWidth(1080);
        outputOption.setHeight(1920);
        outputOption.frameRate = 30;//Output video frame rate, default 30
        outputOption.bitRate = 10;

        EpEditor.merge(epVideos, outputOption, new OnEditorListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Video merged", Toast.LENGTH_SHORT).show();
                });

            }

            @Override
            public void onFailure() {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onProgress(float progress) {

            }
        });
    }

    public void mergeVideos(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("video/*");
        startActivityForResult(intent, 102);
    }

    private String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Video.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);

            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }
}