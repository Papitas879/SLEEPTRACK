package com.example.sleeptrackapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Locale;

public class IniciarSesionActivity extends AppCompatActivity {

    View btnVolver;
    EditText etCorreo, etPassword;
    Button btnEntrar;
    TextView tvOlvidePass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hu03_iniciar_sesion);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnVolver = findViewById(R.id.btn_login_volver);
        etCorreo = findViewById(R.id.et_login_correo);
        etPassword = findViewById(R.id.et_login_password);
        btnEntrar = findViewById(R.id.btn_login_entrar);
        tvOlvidePass = findViewById(R.id.tv_login_olvide_pass);

        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        if (btnEntrar != null) {
            btnEntrar.setOnClickListener(v -> loginUser());
        }
        
        if (tvOlvidePass != null) {
            tvOlvidePass.setOnClickListener(v -> mostrarDialogoRecuperarPassword());
        }
    }

    private void loginUser() {
        String correo = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (correo.isEmpty() || password.isEmpty()) {
            CustomToast.show(this, "Por favor, completa tus credenciales", true);
            return;
        }

        String url = "http://192.168.3.225/SLEEPTRACKAPP/api/login.php";

        JSONObject requestData = new JSONObject();
        try {
            requestData.put("correo", correo);
            requestData.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");
                        
                        if (status.equals("success")) {
                            JSONObject user = response.getJSONObject("user");
                            
                            SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("nombre_usuario", user.getString("nombres"));
                            editor.putString("correo_usuario", correo);
                            
                            if (user.has("hora_dormir") && !user.isNull("hora_dormir") && !user.getString("hora_dormir").isEmpty()) {
                                editor.putString("hora_dormir", user.getString("hora_dormir"));
                                editor.putString("hora_despertar", user.getString("hora_despertar"));
                                
                                String meta_horas = user.getString("meta_horas");
                                editor.putString("meta_horas", meta_horas);
                                editor.putString("duracion_calculada_texto", meta_horas);
                                
                                int[] dur = SleepTimeUtils.calcularHorasYMinutos(user.getString("hora_dormir"), user.getString("hora_despertar"));
                                editor.putInt("duracion_minutos_totales", dur[0] * 60 + dur[1]);
                                editor.putString("duracion_calculada_corto", SleepTimeUtils.formatearDuracionCorto(dur[0], dur[1]));
                            }
                            
                            editor.apply();

                            CustomToast.show(IniciarSesionActivity.this, "¡Bienvenido " + user.getString("nombres") + "!", false);
                            
                            Intent intent = new Intent(IniciarSesionActivity.this, ResumenDescansoActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            CustomToast.show(IniciarSesionActivity.this, message, true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        CustomToast.show(IniciarSesionActivity.this, "Error conectando con el servidor", true);
                    }
                },
                error -> CustomToast.show(IniciarSesionActivity.this, "Error de red. Revisa tu conexión", true)
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void mostrarDialogoRecuperarPassword() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_hu04_recuperar_password, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etCorreoRec = dialogView.findViewById(R.id.et_recuperar_correo);
        TextView tvFechaRec = dialogView.findViewById(R.id.tv_recuperar_fecha);
        EditText etNuevaPass = dialogView.findViewById(R.id.et_recuperar_password);
        Button btnCancelar = dialogView.findViewById(R.id.btn_recuperar_cancelar);
        Button btnConfirmar = dialogView.findViewById(R.id.btn_recuperar_confirmar);

        if (tvFechaRec != null) {
            tvFechaRec.setOnClickListener(v -> {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                int anio = cal.get(java.util.Calendar.YEAR) - 20;
                int mes = cal.get(java.util.Calendar.MONTH);
                int dia = cal.get(java.util.Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        (view, year, month, dayOfMonth) -> {
                            String fechaStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                            tvFechaRec.setText(fechaStr);
                            tvFechaRec.setTextColor(getColor(R.color.text_primary));
                        }, anio, mes, dia);
                datePickerDialog.show();
            });
        }

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(v -> {
                String correo = etCorreoRec.getText().toString().trim();
                String fecha = tvFechaRec.getText().toString().trim();
                String nuevaPass = etNuevaPass.getText().toString().trim();

                if (correo.isEmpty() || fecha.equals("DD/MM/AAAA") || nuevaPass.isEmpty()) {
                    CustomToast.show(this, "Completa todos los campos de recuperación", true);
                    return;
                }

                String url = "http://192.168.3.225/SLEEPTRACKAPP/api/recuperar.php";

                JSONObject requestData = new JSONObject();
                try {
                    requestData.put("correo", correo);
                    requestData.put("fecha_nacimiento", fecha);
                    requestData.put("nueva_password", nuevaPass);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                btnConfirmar.setEnabled(false);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                        response -> {
                            btnConfirmar.setEnabled(true);
                            try {
                                String status = response.getString("status");
                                String message = response.getString("message");
                                
                                if (status.equals("success")) {
                                    CustomToast.show(this, message, false);
                                    dialog.dismiss();
                                } else {
                                    CustomToast.show(this, message, true);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            btnConfirmar.setEnabled(true);
                            CustomToast.show(this, "Error de red al recuperar contraseña", true);
                        }
                );

                Volley.newRequestQueue(this).add(jsonObjectRequest);
            });
        }

        dialog.show();
    }
}