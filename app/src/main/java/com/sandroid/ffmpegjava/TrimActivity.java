package com.sandroid.ffmpegjava;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.io.IOException;

import VideoHandle.EpEditor;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;

public class TrimActivity extends AppCompatActivity {

    Uri uri, contentUri;
    ImageView imageView;
    VideoView videoView;
    TextView tvLeft, tvRight;
    RangeSeekBar rangeSeekBar;
    boolean isPlaying = false;

    int duration;
    String filePrefix;
    String[] command;
    File dest;
    String original_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim);

        imageView = findViewById(R.id.pause);
        videoView = findViewById(R.id.videoView);
        tvLeft = findViewById(R.id.tvLeft);
        tvRight = findViewById(R.id.tvRight);
        rangeSeekBar = findViewById(R.id.seekbar);

        Intent i = getIntent();
        if (i != null) {
            String getUri = i.getStringExtra("uri");
            uri = Uri.parse(getUri);

            videoView.setVideoURI(uri);
            videoView.start();
        }

        setListener();
    }

    private void setListener() {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying) {
                    imageView.setImageResource(R.drawable.ic_play);
                    videoView.pause();
                    isPlaying = false;
                } else {
                    videoView.start();
                    imageView.setImageResource(R.drawable.ic_baseline_pause_24);
                    isPlaying = true;

                }
            }
        });


        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                duration = mediaPlayer.getDuration() / 1000;
                tvLeft.setText("00:00:00");
                tvRight.setText(getTime(mediaPlayer.getDuration() / 1000));
                mediaPlayer.setLooping(true);
                rangeSeekBar.setRangeValues(0, duration);
                rangeSeekBar.setSelectedMaxValue(duration);
                rangeSeekBar.setSelectedMinValue(0);
                rangeSeekBar.setEnabled(true);
                rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                        videoView.seekTo((int) minValue * 1000);
                        tvLeft.setText(getTime((int) bar.getSelectedMinValue()));
                        tvRight.setText(getTime((int) bar.getSelectedMaxValue()));
                    }
                });
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (videoView.getCurrentPosition() >= rangeSeekBar.getSelectedMaxValue().intValue() * 1000)
                            videoView.seekTo(rangeSeekBar.getSelectedMinValue().intValue() * 1000);

                    }
                }, 1000);
            }
        });
    }

    private String getTime(int seconds) {
        int hr = seconds / 3600;
        int rem = seconds % 3600;
        int mn = rem / 60;
        int sec = rem % 60;
        return String.format("%02d", hr) + ":" + String.format("%02d", mn) + ":" + String.format("%02d", sec);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.trim) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(TrimActivity.this);
            LinearLayout layout = new LinearLayout(TrimActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(50, 0, 50, 100);
            final EditText input = new EditText(TrimActivity.this);
            input.setLayoutParams(params);
            input.setGravity(Gravity.TOP | Gravity.START);
            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            layout.addView(input, params);

            alert.setMessage("Set video name");
            alert.setTitle("Change video name");
            alert.setView(layout);
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    filePrefix = input.getText().toString();

                    try {
                        trimVideo(rangeSeekBar.getSelectedMinValue().intValue() * 1000,
                                rangeSeekBar.getSelectedMaxValue().intValue() * 1000,
                                filePrefix);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


//                    Intent intent = new Intent(TrimActivity.this, ProgressBarActivity.class);
//                    intent.putExtra("duration", duration);
//                    intent.putExtra("command", command);
//                    intent.putExtra("destination", dest.getAbsolutePath());
//                    startActivity(intent);
//                    finish();

                    dialogInterface.dismiss();

                }
            });
            alert.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void trimVideo(int startMs, int enMs, String fileName) throws IOException {

       /* if (SDK_INT < Build.VERSION_CODES.Q) {
            storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "trims");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            dest = new File(storageDir, fileName + ".mp4");
            dest.createNewFile();

        } else {
            storageDir =getExternalFilesDir(Environment.DIRECTORY_PICTURES + File.separator + "trims");
            dest = File.createTempFile(fileName, ".mp4", storageDir);
            String currentPhotoPath = dest.getAbsolutePath();
            Toast.makeText(this, currentPhotoPath, Toast.LENGTH_SHORT).show();
        }*/
        /*original_path = getRealPathFromUri(getApplicationContext(), uri);
        duration = (enMs - startMs) / 1000;
        command = new String[]{"-ss", "" + startMs / 1000, "-y", "-i", original_path, "-t", "" + (enMs - startMs) / 1000, "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000",
                "-ac", "2", "-ar", "22050", dest.getAbsolutePath()};*/

       /* File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+File.separator+"my_videos");

        if (!folder.exists()) {
            folder.mkdir();
        }
        filePrefix = fileName;
        String fileExt = ".mp4";
        dest = new File(folder, filePrefix + fileExt);
        dest.createNewFile();

        original_path = getRealPathFromUri(getApplicationContext(), uri);
        duration=(enMs-startMs)/1000;
        command= new String[]{"-ss",""+startMs/1000 ,"-y","-i",original_path,"-t",""+(enMs-startMs)/1000,"-vcodec","mpeg4","-b:v","2097152","-b:a","48000",
        "-ac","2","-ar","22050", dest.getAbsolutePath()};*/

        //File folder=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+"TrimVideos");

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "VideoEdit/Trim/");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        filePrefix = fileName;
        String fileExt = ".mp4";
        dest = new File(folder, filePrefix + fileExt);
        original_path = getRealPathFromUri(getApplicationContext(), uri);
        duration = (enMs - startMs) / 1000;

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Trimming video");
        progressDialog.show();
        progressDialog.setCancelable(false);

        EpVideo epVideo = new EpVideo(original_path);
        epVideo.clip(startMs / 1000f, duration);
        EpEditor.exec(epVideo, new EpEditor.OutputOption(dest.getAbsolutePath()), new OnEditorListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(TrimActivity.this, "Video trimmed", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                });

            }

            @Override
            public void onFailure() {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(TrimActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                });
            }

            @Override
            public void onProgress(float progress) {

            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}