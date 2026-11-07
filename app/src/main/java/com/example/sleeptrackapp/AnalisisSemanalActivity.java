package com.example.sleeptrackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

/**
 * HU-15: Resumen semanal con gráficos de barras interactivos.
 * HU-16: Comparativa de horarios en semana vs. fin de semana y detección de irregularidades.
 */
public class AnalisisSemanalActivity extends AppCompatActivity {

    View ivVolver, btnIrCalendario;
    ImageView btnSemanaAnterior, btnSemanaSiguiente;
    TextView tvSemanaVal, tvPromedioVal, tvCumplimientoVal, tvDiasMedidosDesc, tvMetaIndicador;
    TextView tvSemVal, tvFindeVal, tvIrregularidadMsg;
    View llIrregularidad;

    // Elementos de las 7 barras (Lun - Dom)
    private View[] vBars = new View[7];
    private TextView[] tvBarVals = new TextView[7];
    private TextView[] tvBarLabels = new TextView[7];

    private int offsetSemanas = 0; // 0 = actual, -1 = semana anterior

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hu15_hu16_analisis_semanal);

        ivVolver = findViewById(R.id.iv_hu15_volver);
        btnIrCalendario = findViewById(R.id.btn_hu15_ir_calendario);
        btnSemanaAnterior = findViewById(R.id.btn_semana_anterior);
        btnSemanaSiguiente = findViewById(R.id.btn_semana_siguiente);
        tvSemanaVal = findViewById(R.id.tv_hu15_semana_val);
        tvPromedioVal = findViewById(R.id.tv_hu15_promedio_val);
        tvCumplimientoVal = findViewById(R.id.tv_hu15_cumplimiento_val);
        tvDiasMedidosDesc = findViewById(R.id.tv_hu15_dias_medidos_desc);
        tvMetaIndicador = findViewById(R.id.tv_hu15_meta_indicador);
        tvSemVal = findViewById(R.id.tv_hu16_sem_val);
        tvFindeVal = findViewById(R.id.tv_hu16_finde_val);
        llIrregularidad = findViewById(R.id.ll_hu16_irregularidad);
        tvIrregularidadMsg = findViewById(R.id.tv_hu16_irregularidad_msg);

        // Mapeo de vistas para las 7 barras
        for (int i = 0; i < 7; i++) {
            int barId = getResources().getIdentifier("v_bar_" + i, "id", getPackageName());
            int valId = getResources().getIdentifier("tv_bar_val_" + i, "id", getPackageName());
            int labelId = getResources().getIdentifier("tv_bar_label_" + i, "id", getPackageName());
            vBars[i] = findViewById(barId);
            tvBarVals[i] = findViewById(valId);
            tvBarLabels[i] = findViewById(labelId);
        }

        if (ivVolver != null) {
            ivVolver.setOnClickListener(v -> volverAHistorial());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                volverAHistorial();
            }
        });

        if (btnIrCalendario != null) {
            btnIrCalendario.setOnClickListener(v -> {
                Intent intent = new Intent(this, HistorialMensualActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (btnSemanaAnterior != null) {
            btnSemanaAnterior.setOnClickListener(v -> {
                offsetSemanas--;
                cargarAnalisisSemanal();
            });
        }

        if (btnSemanaSiguiente != null) {
            btnSemanaSiguiente.setOnClickListener(v -> {
                if (offsetSemanas < 0) {
                    offsetSemanas++;
                    cargarAnalisisSemanal();
                } else {
                    CustomToast.show(this, "Ya estás en la semana en curso", false);
                }
            });
        }

        cargarAnalisisSemanal();

        BottomNavHelper.setup(this, R.id.nav_historial);
    }

    private void cargarAnalisisSemanal() {
        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        String correo = prefs.getString("correo_usuario", "");

        if (correo.isEmpty()) {
            // Datos de demostración offline
            aplicarDatosDemo();
            return;
        }

        String url = "http://192.168.3.225/SLEEPTRACKAPP/api/obtener_analisis_semanal.php";
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("correo", correo);
            requestData.put("offset_semanas", offsetSemanas);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            procesarRespuestaSemanal(response);
                        } else {
                            aplicarDatosDemo();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        aplicarDatosDemo();
                    }
                },
                error -> aplicarDatosDemo()
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void procesarRespuestaSemanal(JSONObject json) {
        try {
            String etiqueta = json.optString("etiqueta_semana", "Semana actual");
            String promedioTxt = json.optString("promedio_texto", "7h 15m");
            String cumplimientoRatio = json.optString("cumplimiento_ratio", "5 de 7");
            String habitoSemana = json.optString("habito_semana", "~23:30 a 07:15");
            String habitoFinde = json.optString("habito_finde", "~01:00 a 09:30");
            boolean irregularidad = json.optBoolean("irregularidad_detectada", false);
            int diasMedidos = json.optInt("dias_medidos", 0);
            String metaTexto = json.optString("meta_texto", "8h");

            if (tvSemanaVal != null) tvSemanaVal.setText(etiqueta);
            if (tvPromedioVal != null) tvPromedioVal.setText(promedioTxt);
            if (tvCumplimientoVal != null) tvCumplimientoVal.setText(cumplimientoRatio);
            if (tvDiasMedidosDesc != null) {
                if (diasMedidos == 0) {
                    tvDiasMedidosDesc.setText("Sin registros medidos\nesta semana");
                } else {
                    tvDiasMedidosDesc.setText("Promedio por noche\nen " + diasMedidos + (diasMedidos == 1 ? " día medido" : " días medidos"));
                }
            }
            if (tvSemVal != null) tvSemVal.setText(habitoSemana);
            if (tvFindeVal != null) tvFindeVal.setText(habitoFinde);
            if (tvMetaIndicador != null) tvMetaIndicador.setText("Meta: " + metaTexto);

            if (llIrregularidad != null) {
                llIrregularidad.setVisibility(irregularidad ? View.VISIBLE : View.GONE);
            }

            JSONArray diasArr = json.optJSONArray("dias");
            if (diasArr != null && diasArr.length() >= 7) {
                float maxAlturaDp = 130f;
                float densidad = getResources().getDisplayMetrics().density;
                int maxPx = (int) (maxAlturaDp * densidad);

                for (int i = 0; i < 7; i++) {
                    JSONObject dia = diasArr.getJSONObject(i);
                    double horas = dia.optDouble("duracion_horas", 0.0);
                    boolean cumplio = dia.optBoolean("cumplio", false);
                    boolean tieneReg = dia.optBoolean("tiene_registro", false);
                    String diaNombre = dia.optString("dia_nombre", "");

                    if (!tieneReg) {
                        if (tvBarVals[i] != null) {
                            tvBarVals[i].setText("-");
                            tvBarVals[i].setTextColor(ContextCompat.getColor(this, R.color.text_subdued));
                        }
                        if (tvBarLabels[i] != null) {
                            tvBarLabels[i].setText(diaNombre.length() > 0 ? diaNombre.substring(0, 1) : "");
                        }
                        if (vBars[i] != null) {
                            int alturaPx = (int) (6 * densidad);
                            ViewGroup.LayoutParams lp = vBars[i].getLayoutParams();
                            lp.height = alturaPx;
                            vBars[i].setLayoutParams(lp);
                            vBars[i].setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.card_surface)));
                            final String infoDia = diaNombre + ": Sin registro de descanso";
                            vBars[i].setOnClickListener(v -> CustomToast.show(this, infoDia, false));
                        }
                    } else {
                        if (tvBarVals[i] != null) {
                            tvBarVals[i].setText(String.format(Locale.getDefault(), "%.1fh", horas));
                            tvBarVals[i].setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                        }
                        if (tvBarLabels[i] != null) {
                            tvBarLabels[i].setText(diaNombre.length() > 0 ? diaNombre.substring(0, 1) : "");
                        }
                        if (vBars[i] != null) {
                            int alturaPx = (int) Math.max(18 * densidad, (horas / 10.0) * maxPx);
                            ViewGroup.LayoutParams lp = vBars[i].getLayoutParams();
                            lp.height = alturaPx;
                            vBars[i].setLayoutParams(lp);
                            if (cumplio) {
                                vBars[i].setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accent_mint)));
                            } else {
                                vBars[i].setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));
                            }
                            final String infoDia = diaNombre + ": " + String.format(Locale.getDefault(), "%.1f horas", horas) + (cumplio ? " (Meta lograda)" : "");
                            vBars[i].setOnClickListener(v -> CustomToast.show(this, infoDia, false));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void aplicarDatosDemo() {
        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        int durMin = prefs.getInt("ultima_duracion_minutos", 0);
        int targetMin = prefs.getInt("duracion_minutos_totales", 480);
        String metaTexto = prefs.getString("meta_horas", "8 horas");

        int hProm = durMin / 60;
        int mProm = durMin % 60;
        String promTxt = durMin > 0 ? (hProm + "h " + String.format(Locale.getDefault(), "%02dm", mProm)) : "0h 00m";

        if (tvSemanaVal != null) tvSemanaVal.setText("Semana actual");
        if (tvPromedioVal != null) tvPromedioVal.setText(promTxt);
        if (tvCumplimientoVal != null) tvCumplimientoVal.setText(durMin >= targetMin * 0.9 ? "1 de 1" : "0 de 1");
        if (tvDiasMedidosDesc != null) tvDiasMedidosDesc.setText(durMin > 0 ? "Promedio por noche\nen 1 día medido" : "Sin registros esta semana");
        if (tvSemVal != null) tvSemVal.setText(durMin > 0 ? ("~" + promTxt + " promedio") : "Sin registros");
        if (tvFindeVal != null) tvFindeVal.setText("Sin registros");
        if (llIrregularidad != null) llIrregularidad.setVisibility(View.GONE);

        String[] labels = {"L", "M", "M", "J", "V", "S", "D"};
        float densidad = getResources().getDisplayMetrics().density;
        int maxPx = (int) (130 * densidad);

        // Día actual de la semana (en demo marcar el viernes o hoy)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int diaSem = (cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7; // Lunes = 0, Domingo = 6

        for (int i = 0; i < 7; i++) {
            if (i == diaSem && durMin > 0) {
                float h = Math.round((durMin / 60.0f) * 10.0f) / 10.0f;
                boolean cumplio = durMin >= targetMin * 0.9;
                if (tvBarVals[i] != null) {
                    tvBarVals[i].setText(String.format(Locale.getDefault(), "%.1fh", h));
                    tvBarVals[i].setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                }
                if (tvBarLabels[i] != null) tvBarLabels[i].setText(labels[i]);
                if (vBars[i] != null) {
                    int alturaPx = (int) Math.max(18 * densidad, (h / 10.0f) * maxPx);
                    ViewGroup.LayoutParams lp = vBars[i].getLayoutParams();
                    lp.height = alturaPx;
                    vBars[i].setLayoutParams(lp);
                    vBars[i].setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, cumplio ? R.color.accent_mint : R.color.primary)));
                    final int idx = i;
                    vBars[i].setOnClickListener(v -> CustomToast.show(this, labels[idx] + ": " + h + "h", false));
                }
            } else {
                if (tvBarVals[i] != null) {
                    tvBarVals[i].setText("-");
                    tvBarVals[i].setTextColor(ContextCompat.getColor(this, R.color.text_subdued));
                }
                if (tvBarLabels[i] != null) tvBarLabels[i].setText(labels[i]);
                if (vBars[i] != null) {
                    int alturaPx = (int) (6 * densidad);
                    ViewGroup.LayoutParams lp = vBars[i].getLayoutParams();
                    lp.height = alturaPx;
                    vBars[i].setLayoutParams(lp);
                    vBars[i].setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.card_surface)));
                    final int idx = i;
                    vBars[i].setOnClickListener(v -> CustomToast.show(this, labels[idx] + ": Sin registro", false));
                }
            }
        }
    }

    private void volverAHistorial() {
        Intent intent = new Intent(this, HistorialMensualActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
