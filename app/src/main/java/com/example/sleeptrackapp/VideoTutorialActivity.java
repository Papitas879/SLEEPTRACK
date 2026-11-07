package com.example.sleeptrackapp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class VideoTutorialActivity extends AppCompatActivity {

    private VideoView videoView;
    private TextView btnSaltar;
    private MaterialButton btnContinuar, btnRepetir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video_tutorial);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_tutorial), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        videoView = findViewById(R.id.vv_tutorial);
        btnSaltar = findViewById(R.id.btn_saltar_tutorial);
        btnContinuar = findViewById(R.id.btn_continuar_tutorial);
        btnRepetir = findViewById(R.id.btn_repetir_tutorial);

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tutorial_sleeptrack);
        videoView.setVideoURI(videoUri);

        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            videoView.start();
        });

        videoView.setOnCompletionListener(mp -> {
            if (btnContinuar != null) {
                btnContinuar.setText("Continuar  →");
            }
        });

        if (btnSaltar != null) {
            btnSaltar.setOnClickListener(v -> irACrearPerfil());
        }

        if (btnContinuar != null) {
            btnContinuar.setOnClickListener(v -> irACrearPerfil());
        }

        if (btnRepetir != null) {
            btnRepetir.setOnClickListener(v -> {
                videoView.seekTo(0);
                videoView.start();
            });
        }
    }

    private void irACrearPerfil() {
        if (videoView != null && videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        Intent intent = new Intent(VideoTutorialActivity.this, CrearPerfilActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null && !videoView.isPlaying()) {
            videoView.start();
        }
    }
}
