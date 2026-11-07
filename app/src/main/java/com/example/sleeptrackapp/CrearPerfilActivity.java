package com.example.sleeptrackapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

import java.util.Locale;

public class CrearPerfilActivity extends AppCompatActivity {

    View ivVolver;
    EditText etNombres, etCorreo, etPassword;
    TextView tvFechaNacSelector, tvUnivSelector, tvCarreraSelector;
    Button btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hu02_crear_perfil);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivVolver = findViewById(R.id.iv_hu02_volver);
        etNombres = findViewById(R.id.et_hu02_nombres);
        etCorreo = findViewById(R.id.et_hu02_correo);
        etPassword = findViewById(R.id.et_hu02_password);
        tvFechaNacSelector = findViewById(R.id.tv_hu02_fecha_nac_selector);
        tvUnivSelector = findViewById(R.id.tv_hu02_universidad_selector);
        tvCarreraSelector = findViewById(R.id.tv_hu02_carrera_selector);
        btnContinuar = findViewById(R.id.btn_hu02_continuar);

        if (ivVolver != null) {
            ivVolver.setOnClickListener(v -> finish());
        }

        if (tvFechaNacSelector != null) {
            tvFechaNacSelector.setOnClickListener(v -> mostrarDatePicker(tvFechaNacSelector));
        }
        if (tvUnivSelector != null) {
            tvUnivSelector.setOnClickListener(v -> mostrarDialogoSelector(tvUnivSelector, R.array.universidades, "Selecciona tu universidad"));
        }
        if (tvCarreraSelector != null) {
            tvCarreraSelector.setOnClickListener(v -> mostrarDialogoSelector(tvCarreraSelector, R.array.carreras, "Selecciona tu carrera"));
        }

        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);

        if (btnContinuar != null) {
            btnContinuar.setOnClickListener(v -> {
                String nombres = etNombres != null ? etNombres.getText().toString().trim() : "";
                String correo = etCorreo != null ? etCorreo.getText().toString().trim() : "";
                String password = etPassword != null ? etPassword.getText().toString().trim() : "";

                if (nombres.isEmpty() || correo.isEmpty() || password.isEmpty()) {
                    CustomToast.show(this, "Por favor, completa tus datos principales", true);
                    return;
                }

                String fechaNac = tvFechaNacSelector != null ? tvFechaNacSelector.getText().toString() : "";
                String universidad = tvUnivSelector != null ? tvUnivSelector.getText().toString() : "";
                String carrera = tvCarreraSelector != null ? tvCarreraSelector.getText().toString() : "";

                String url = "http://192.168.3.225/SLEEPTRACKAPP/api/register.php";

                JSONObject requestData = new JSONObject();
                try {
                    requestData.put("nombres", nombres);
                    requestData.put("correo", correo);
                    requestData.put("password", password);
                    requestData.put("fecha_nacimiento", fechaNac);
                    requestData.put("universidad", universidad);
                    requestData.put("carrera", carrera);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                btnContinuar.setEnabled(false);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                        response -> {
                            btnContinuar.setEnabled(true);
                            try {
                                String status = response.getString("status");
                                String message = response.getString("message");

                                if (status.equals("success")) {

                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("nombre_usuario", nombres);
                                    editor.putString("correo_usuario", correo);
                                    editor.putString("fecha_nac_usuario", fechaNac);
                                    editor.putString("universidad_usuario", universidad);
                                    editor.putString("carrera_usuario", carrera);
                                    editor.apply();

                                    CustomToast.show(this, "Perfil creado exitosamente", false);

                                    Intent intent = new Intent(CrearPerfilActivity.this, ConsentimientoActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    CustomToast.show(this, message, true);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                CustomToast.show(this, "Error conectando con el servidor", true);
                            }
                        },
                        error -> {
                            btnContinuar.setEnabled(true);
                            CustomToast.show(this, "Error de red. Revisa tu conexiÃ³n a internet", true);
                        }
                );

                Volley.newRequestQueue(this).add(jsonObjectRequest);
            });
        }
    }

    private void mostrarDialogoSelector(TextView targetView, int arrayResId, String titulo) {
        String[] opciones = getResources().getStringArray(arrayResId);
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(titulo)
                .setItems(opciones, (dialog, which) -> {
                    targetView.setText(opciones[which]);
                    targetView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                })
                .show();
    }

    private void mostrarDatePicker(TextView targetView) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int anio = cal.get(java.util.Calendar.YEAR) - 20;
        int mes = cal.get(java.util.Calendar.MONTH);
        int dia = cal.get(java.util.Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String fechaStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    targetView.setText(fechaStr);
                    targetView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                }, anio, mes, dia);
        datePickerDialog.show();
    }
}
