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

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResumenDescansoActivity extends AppCompatActivity {

    TextView tvTitulo, tvFecha, tvDuracionEstimada, tvCumplimiento;
    TextView tvInicioVal, tvDespertarVal, tvEficienciaVal;
    TextView tvCalidadVal, tvInterrupcionesVal, tvInteraccionesVal, tvMetaEst;
    Button btnIniciarMonitoreo, btnRegistrarComoDormi, btnVerGraficos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hu12_hu13_hu14_resumen_descanso);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTitulo = findViewById(R.id.tv_hu10_titulo);
        tvFecha = findViewById(R.id.tv_hu10_fecha);
        tvDuracionEstimada = findViewById(R.id.tv_hu10_duracion_estimada);
        tvCumplimiento = findViewById(R.id.tv_hu10_cumplimiento);
        tvInicioVal = findViewById(R.id.tv_hu10_inicio_val);
        tvDespertarVal = findViewById(R.id.tv_hu10_despertar_val);
        tvEficienciaVal = findViewById(R.id.tv_hu10_eficiencia_val);
        tvCalidadVal = findViewById(R.id.tv_hu10_calidad_val);
        tvInterrupcionesVal = findViewById(R.id.tv_hu10_interrupciones_val);
        tvInteraccionesVal = findViewById(R.id.tv_hu10_interacciones_val);
        tvMetaEst = findViewById(R.id.tv_hu10_meta_est);

        btnIniciarMonitoreo = findViewById(R.id.btn_iniciar_monitoreo);
        btnRegistrarComoDormi = findViewById(R.id.btn_registrar_como_dormi);
        btnVerGraficos = findViewById(R.id.btn_ver_graficos);

        if (btnIniciarMonitoreo != null) {
            btnIniciarMonitoreo.setOnClickListener(v -> startActivity(new Intent(this, MonitoreoActivity.class)));
        }

        if (btnRegistrarComoDormi != null) {
            btnRegistrarComoDormi.setOnClickListener(v -> startActivity(new Intent(this, RegistroDiarioActivity.class)));
        }

        if (btnVerGraficos != null) {
            btnVerGraficos.setOnClickListener(v -> startActivity(new Intent(this, AnalisisSemanalActivity.class)));
        }

        androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh = findViewById(R.id.swipe_refresh_resumen);
        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent_mint);
            swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.card_surface);
            swipeRefresh.setOnRefreshListener(() -> {
                cargarResumenData();
                swipeRefresh.setRefreshing(false);
            });
        }

        cargarResumenData();

        BottomNavHelper.setup(this, R.id.nav_inicio);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarResumenData();
    }

    private void cargarResumenData() {
        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        String correo = prefs.getString("correo_usuario", "");

        // 1. Cargar datos locales de caché para respuesta instantánea (Apple HIG Zero-Latency)
        long inicioMillis = prefs.getLong("ultimo_monitoreo_inicio", 0);
        long finMillis = prefs.getLong("ultimo_monitoreo_fin", 0);
        int movimientos = prefs.getInt("movimientos_detectados", 3);
        int targetMinutos = prefs.getInt("duracion_minutos_totales", 480);
        String metaHorasTexto = prefs.getString("meta_horas", "8 horas");
        int interaccionesPantalla = prefs.getInt("interacciones_pantalla", 0);

        if (tvMetaEst != null) {
            tvMetaEst.setText("Meta: " + metaHorasTexto);
        }

        // Interacciones funcionales con la pantalla
        if (tvInteraccionesVal != null) {
            if (interaccionesPantalla == 0) {
                tvInteraccionesVal.setText("0 eventos (Excelente)");
                tvInteraccionesVal.setTextColor(getColor(R.color.accent_mint));
            } else if (interaccionesPantalla <= 2) {
                tvInteraccionesVal.setText(interaccionesPantalla + (interaccionesPantalla == 1 ? " evento" : " eventos"));
                tvInteraccionesVal.setTextColor(getColor(R.color.warning));
            } else {
                tvInteraccionesVal.setText(interaccionesPantalla + " eventos (Uso alto)");
                tvInteraccionesVal.setTextColor(getColor(R.color.error));
            }
        }

        if (inicioMillis > 0 && finMillis > inicioMillis) {
            long duracionMillis = finMillis - inicioMillis;
            int duracionMinutosReales = (int) (duracionMillis / (1000 * 60));
            if (duracionMinutosReales <= 0 && duracionMillis >= 25000) {
                duracionMinutosReales = 1;
            } else if (duracionMinutosReales < 0) {
                duracionMinutosReales = 0;
            }

            int horas = duracionMinutosReales / 60;
            int mins = duracionMinutosReales % 60;
            String durTexto = SleepTimeUtils.formatearDuracionCorto(horas, mins);

            SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String horaInicio = sdfHora.format(new Date(inicioMillis));
            String horaFin = sdfHora.format(new Date(finMillis));

            int calidadPorcentaje = SleepTimeUtils.calcularCalidadPorcentaje(duracionMinutosReales, targetMinutos, movimientos);
            int eficienciaPorcentaje = SleepTimeUtils.calcularEficiencia(duracionMinutosReales, movimientos);
            String clasificacionCalidad = SleepTimeUtils.clasificarCalidad(calidadPorcentaje);

            if (tvInicioVal != null) tvInicioVal.setText(horaInicio);
            if (tvDespertarVal != null) tvDespertarVal.setText(horaFin);
            if (tvDuracionEstimada != null) tvDuracionEstimada.setText(durTexto);
            if (tvCumplimiento != null) tvCumplimiento.setText(calidadPorcentaje + "%");
            if (tvEficienciaVal != null) tvEficienciaVal.setText(eficienciaPorcentaje + "%");
            if (tvCalidadVal != null) tvCalidadVal.setText(getEmojiForCalidad(clasificacionCalidad) + " " + clasificacionCalidad);
        } else {
            String hDormir = prefs.getString("hora_dormir", "23:00");
            String hDespertar = prefs.getString("hora_despertar", "07:00");

            if (tvInicioVal != null) tvInicioVal.setText(hDormir);
            if (tvDespertarVal != null) tvDespertarVal.setText(hDespertar);
            if (tvDuracionEstimada != null) tvDuracionEstimada.setText("0h 00m");
            if (tvCumplimiento != null) tvCumplimiento.setText("0%");
            if (tvEficienciaVal != null) tvEficienciaVal.setText("0%");
            if (tvCalidadVal != null) tvCalidadVal.setText("Sin registro");
        }

        if (correo.isEmpty()) return;

        // 2. Sincronizar datos frescos desde la BD MySQL en XAMPP
        String url = "http://192.168.3.225/SLEEPTRACKAPP/api/obtener_resumen.php";
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("correo", correo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success") && response.has("data")) {
                            JSONObject data = response.getJSONObject("data");

                            if (data.has("duracion_minutos")) {
                                int durMin = data.optInt("duracion_minutos", 0);
                                int h = durMin / 60;
                                int m = durMin % 60;
                                if (tvDuracionEstimada != null) tvDuracionEstimada.setText(h + "h " + String.format(Locale.getDefault(), "%02dm", m));
                            }

                            if (data.has("calidad_porcentaje") && tvCumplimiento != null) {
                                int calPct = data.optInt("calidad_porcentaje", 0);
                                tvCumplimiento.setText(calPct + "%");
                            }

                            if (data.has("eficiencia") && tvEficienciaVal != null) {
                                int efPct = data.optInt("eficiencia", 0);
                                tvEficienciaVal.setText(efPct + "%");
                            }

                            String hIni = data.optString("hora_inicio", "");
                            if (!hIni.isEmpty() && tvInicioVal != null) {
                                tvInicioVal.setText(hIni);
                            }

                            String hFin = data.optString("hora_fin", "");
                            if (!hFin.isEmpty() && tvDespertarVal != null) {
                                tvDespertarVal.setText(hFin);
                            }

                            if (data.has("calidad") && tvCalidadVal != null) {
                                String cal = data.getString("calidad");
                                tvCalidadVal.setText(getEmojiForCalidad(cal) + " " + cal);
                            }

                            if (data.has("interrupciones") && tvInterrupcionesVal != null) {
                                tvInterrupcionesVal.setText(data.getString("interrupciones"));
                            }

                            // Pantallas o eventos nocturnos
                            String pan = data.optString("pantallas", "");
                            if (!pan.isEmpty() && tvInteraccionesVal != null) {
                                tvInteraccionesVal.setText(pan);
                                if (pan.contains("0") || pan.contains("Menos")) {
                                    tvInteraccionesVal.setTextColor(getColor(R.color.accent_mint));
                                } else if (pan.contains("Más") || pan.contains("alto")) {
                                    tvInteraccionesVal.setTextColor(getColor(R.color.error));
                                }
                            }

                            if (data.has("fecha") && tvFecha != null) {
                                tvFecha.setText("Noche del " + data.getString("fecha"));
                            }

                            if (data.has("target_meta_horas") && tvMetaEst != null) {
                                tvMetaEst.setText("Meta: " + data.getString("target_meta_horas"));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {}
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private String getEmojiForCalidad(String calidad) {
        if (calidad == null) return "😐";
        switch (calidad) {
            case "Muy mala": return "😫";
            case "Mala": return "🙁";
            case "Buena": return "🙂";
            case "Muy buena": return "🤩";
            default: return "😐";
        }
    }
}
