package com.example.sleeptrackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BienvenidaActivity extends AppCompatActivity {

    TextView tvAppName, tvTitulo, tvDesc, tvComenzar;
    Button btnYaTengoCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        String correoGuardado = prefs.getString("correo_usuario", "");
        if (!correoGuardado.isEmpty()) {
            Intent intent = new Intent(BienvenidaActivity.this, ResumenDescansoActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hu01_bienvenida);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvAppName = findViewById(R.id.tv_hu01_app_name);
        tvTitulo = findViewById(R.id.tv_hu01_titulo);
        tvDesc = findViewById(R.id.tv_hu01_desc);
        tvComenzar = findViewById(R.id.tv_hu01_comenzar);
        btnYaTengoCuenta = findViewById(R.id.btn_hu01_ya_tengo_cuenta);

        if (tvComenzar != null) {
            tvComenzar.setOnClickListener(v -> {
                Intent intent = new Intent(BienvenidaActivity.this, CrearPerfilActivity.class);
                startActivity(intent);
            });
        }

        if (btnYaTengoCuenta != null) {
            btnYaTengoCuenta.setOnClickListener(v -> {
                Intent intent = new Intent(BienvenidaActivity.this, IniciarSesionActivity.class);
                startActivity(intent);
            });
        }
    }
}
