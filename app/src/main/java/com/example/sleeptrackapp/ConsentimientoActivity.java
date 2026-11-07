package com.example.sleeptrackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ConsentimientoActivity extends AppCompatActivity {

    CheckBox cbAcepto;
    Button btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hu05_consentimiento_monitoreo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cbAcepto = findViewById(R.id.cb_hu03_acepto);
        btnContinuar = findViewById(R.id.btn_hu03_continuar);

        if (btnContinuar != null) {
            btnContinuar.setOnClickListener(v -> {
                boolean sensoresAceptados = false;
                if (cbAcepto != null) {
                    sensoresAceptados = cbAcepto.isChecked();
                }

                SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
                prefs.edit().putBoolean("sensores_aceptados", sensoresAceptados).apply();

                Intent intent = new Intent(ConsentimientoActivity.this, ConfigurarDescansoActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}
