package com.example.djoman.audioapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static MediaRecorder mRecorder;
    private static MediaPlayer mPlayer;

    File audiofile = null;
    static final String TAG = "MediaRecording";
    private Button mStop;
    private Button mPlay;
    private Button mRecord;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Relie les boutons à leurs attributs
        mRecord = (Button) findViewById(R.id.record);
        mPlay = (Button) findViewById(R.id.play);
        mStop = (Button) findViewById(R.id.stop);

        // Vérifie la mise en fonctionnement du microphone
        if (!hasMicrophone())
        {
            mStop.setEnabled(false);
            mPlay.setEnabled(false);
            mRecord.setEnabled(false);
        } else {
            mPlay.setEnabled(false);
            mStop.setEnabled(false);
        }

        mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    recordAudio(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    playAudio(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    stopClicked(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Retourne un booléen si le microphone fonctionne ou pas
    protected boolean hasMicrophone() {
        PackageManager pmanager = this.getPackageManager();
        return pmanager.hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE);
    }

    protected void addRecordingToMediaLibrary() {
        //creating content values of size 4
        ContentValues values = new ContentValues(4);
        long current = System.currentTimeMillis();
        values.put(MediaStore.Audio.Media.TITLE, "record" + audiofile.getName());
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        values.put(MediaStore.Audio.Media.MIME_TYPE, "record.3gpp");
        values.put(MediaStore.Audio.Media.DATA, audiofile.getAbsolutePath());

        //creating content resolver and storing it in the external content uri
        ContentResolver contentResolver = getContentResolver();
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri newUri = contentResolver.insert(base, values);

        //sending broadcast message to scan the media file so that it can be available
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
        Toast.makeText(this, "Added File " + newUri, Toast.LENGTH_LONG).show();
    }

    public void recordAudio (View view) throws IOException
    {
        isRecording = true;
        mStop.setEnabled(true);
        mPlay.setEnabled(false);
        mRecord.setEnabled(false);

        // Crée un fichier pour déposer les records sur carte SD
        File dir = Environment.getExternalStorageDirectory();

        try {
            audiofile = File.createTempFile("record", ".3gp", dir);
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(audiofile.getAbsolutePath());
            mRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "external storage access error");
        }
        mRecorder.start();
    }

    public void playAudio (View view) throws IOException
    {
        mPlay.setEnabled(false);
        mRecord.setEnabled(false);
        mStop.setEnabled(true);

        mPlayer = new MediaPlayer();
        mPlayer.setDataSource(audiofile.getAbsolutePath());
        mPlayer.prepare();
        mPlayer.start();
    }

    public void stopClicked (View view) throws IOException
    {
        mStop.setEnabled(false);
        mPlay.setEnabled(true);
        mRecord.setEnabled(true);

        if (isRecording)
        {
            mRecorder.stop();
            mRecorder.release();
            addRecordingToMediaLibrary();
            mRecorder = null;
            isRecording = false;
        } else {
            mPlayer.release();
            mPlayer = null;
            mRecord.setEnabled(true);
        }
    }
}
