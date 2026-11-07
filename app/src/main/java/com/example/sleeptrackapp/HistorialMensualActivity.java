package com.example.sleeptrackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HistorialMensualActivity extends AppCompatActivity {

    View ivVolver, btnIrGraficos;
    ImageView btnMesAnterior, btnMesSiguiente;
    TextView tvMesVal, tvMalaCalidad, tvUsoPantalla;
    GridLayout gridCalendario;

    View cardDetalleDia;
    TextView tvDetalleFecha, tvDetalleCalidad, tvDetalleDuracion, tvDetallePantalla;

    private int anioActual;
    private int mesActualIndex;
    private int diaSeleccionado = -1;
    private final Map<Integer, LinearLayout> dayViews = new HashMap<>();
    private final Map<Integer, TextView> dayTextViews = new HashMap<>();
    private final String[] mesesNombres = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private Map<String, JSONObject> registrosPorFecha = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hu17_hu18_historial_mensual);

        Calendar now = Calendar.getInstance();
        anioActual = now.get(Calendar.YEAR);
        mesActualIndex = now.get(Calendar.MONTH);

        ivVolver = findViewById(R.id.iv_hu17_volver);
        btnIrGraficos = findViewById(R.id.btn_hu17_ir_graficos);
        btnMesAnterior = findViewById(R.id.btn_mes_anterior);
        btnMesSiguiente = findViewById(R.id.btn_mes_siguiente);
        tvMesVal = findViewById(R.id.tv_hu17_mes_val);
        tvMalaCalidad = findViewById(R.id.tv_hu18_mala_calidad);
        tvUsoPantalla = findViewById(R.id.tv_hu18_uso_pantalla);
        gridCalendario = findViewById(R.id.grid_calendario_dias);

        cardDetalleDia = findViewById(R.id.card_detalle_dia);
        tvDetalleFecha = findViewById(R.id.tv_detalle_fecha);
        tvDetalleCalidad = findViewById(R.id.tv_detalle_calidad);
        tvDetalleDuracion = findViewById(R.id.tv_detalle_duracion);
        tvDetallePantalla = findViewById(R.id.tv_detalle_pantalla);

        if (cardDetalleDia != null) {
            cardDetalleDia.setOnClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
                Intent intent = new Intent(this, RegistroDiarioActivity.class);
                startActivity(intent);
            });
        }

        if (ivVolver != null) {
            ivVolver.setOnClickListener(v -> volverAInicio());
        }

        if (btnIrGraficos != null) {
            btnIrGraficos.setOnClickListener(v -> {
                Intent intent = new Intent(this, AnalisisSemanalActivity.class);
                startActivity(intent);
                finish();
            });
        }

        SwipeRefreshLayout swipeRefresh = findViewById(R.id.swipe_refresh_historial);
        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent_mint);
            swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.card_surface);
            swipeRefresh.setOnRefreshListener(() -> {
                cargarHistorial();
                swipeRefresh.setRefreshing(false);
            });
        }

        actualizarTituloMes();

        if (btnMesAnterior != null) {
            btnMesAnterior.setOnClickListener(v -> {
                diaSeleccionado = -1;
                if (mesActualIndex > 0) {
                    mesActualIndex--;
                } else {
                    mesActualIndex = 11;
                    anioActual--;
                }
                actualizarTituloMes();
                cargarHistorial();
            });
        }

        if (btnMesSiguiente != null) {
            btnMesSiguiente.setOnClickListener(v -> {
                diaSeleccionado = -1;
                if (mesActualIndex < 11) {
                    mesActualIndex++;
                } else {
                    mesActualIndex = 0;
                    anioActual++;
                }
                actualizarTituloMes();
                cargarHistorial();
            });
        }

        View cardMalaCalidad = findViewById(R.id.card_hu18_mala_calidad);
        if (cardMalaCalidad != null) {
            cardMalaCalidad.setOnClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
                CustomToast.show(this, "Noches con calidad baja o regular en " + mesesNombres[mesActualIndex], false);
            });
        }

        View cardUsoPantalla = findViewById(R.id.card_hu18_uso_pantalla);
        if (cardUsoPantalla != null) {
            cardUsoPantalla.setOnClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
                CustomToast.show(this, "Noches con más de 30 min de celular o encendidos de pantalla.", false);
            });
        }

        cargarHistorial();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                volverAInicio();
            }
        });

        BottomNavHelper.setup(this, R.id.nav_historial);
    }

    private void actualizarTituloMes() {
        if (tvMesVal != null) {
            tvMesVal.setText(mesesNombres[mesActualIndex] + " " + anioActual);
        }
    }

    private void cargarHistorial() {
        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        String correo = prefs.getString("correo_usuario", "");

        cargarRegistroLocalHoy();

        if (correo.isEmpty()) {
            actualizarMetricasResumen();
            construirCalendarioInteractivo();
            return;
        }

        String url = "http://192.168.3.225/SLEEPTRACKAPP/api/obtener_historial.php";

        JSONObject requestData = new JSONObject();
        try {
            requestData.put("correo", correo);
            requestData.put("mes", mesActualIndex + 1);
            requestData.put("anio", anioActual);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success") && response.has("data")) {
                            JSONArray data = response.getJSONArray("data");

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject reg = data.getJSONObject(i);
                                String fecha = reg.optString("fecha", "");
                                if (!fecha.isEmpty()) {
                                    registrosPorFecha.put(fecha, reg);
                                }
                            }
                        }

                        actualizarMetricasResumen();
                        construirCalendarioInteractivo();
                    } catch (Exception e) {
                        e.printStackTrace();
                        actualizarMetricasResumen();
                        construirCalendarioInteractivo();
                    }
                },
                error -> {
                    actualizarMetricasResumen();
                    construirCalendarioInteractivo();
                }
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void cargarRegistroLocalHoy() {
        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        long finMillis = prefs.getLong("ultimo_monitoreo_fin", 0);
        if (finMillis > 0) {
            SimpleDateFormat sdfFecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String hoyStr = sdfFecha.format(new Date(finMillis));
            int durMin = prefs.getInt("ultima_duracion_minutos", 0);
            int calPct = prefs.getInt("ultima_calidad_porcentaje", 0);
            int pant = prefs.getInt("interacciones_pantalla", 0);

            try {
                JSONObject localObj = new JSONObject();
                localObj.put("fecha", hoyStr);
                localObj.put("duracion_minutos", durMin);
                localObj.put("calidad", SleepTimeUtils.clasificarCalidad(calPct));
                localObj.put("calidad_porcentaje", calPct);
                localObj.put("pantallas", pant + " eventos");
                registrosPorFecha.put(hoyStr, localObj);
            } catch (Exception ignored) {}
        }
    }

    private void actualizarMetricasResumen() {
        int malas = 0;
        int pantallasAltas = 0;

        for (JSONObject reg : registrosPorFecha.values()) {
            String cal = reg.optString("calidad", "");
            String pan = reg.optString("pantallas", "");
            int calPct = reg.optInt("calidad_porcentaje", 75);

            if (cal.contains("Mala") || cal.contains("Muy mala") || calPct < 50) {
                malas++;
            }
            if (pan.contains("Más") || pan.contains("60") || pan.contains("alto") || pan.contains("3 eventos") || pan.contains("4") || pan.contains("5")) {
                pantallasAltas++;
            }
        }

        if (tvMalaCalidad != null) {
            tvMalaCalidad.setText(malas + (malas == 1 ? " día reportado" : " días reportados"));
        }
        if (tvUsoPantalla != null) {
            tvUsoPantalla.setText(pantallasAltas + (pantallasAltas == 1 ? " noche con uso alto" : " noches con uso alto"));
        }
    }

    private void construirCalendarioInteractivo() {
        if (gridCalendario == null) return;
        gridCalendario.removeAllViews();
        dayViews.clear();
        dayTextViews.clear();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, anioActual);
        cal.set(Calendar.MONTH, mesActualIndex);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int maxDiasMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int primerDiaSemana = cal.get(Calendar.DAY_OF_WEEK);
        int offsetLunes = (primerDiaSemana == Calendar.SUNDAY) ? 6 : primerDiaSemana - 2;

        float densidad = getResources().getDisplayMetrics().density;
        int tamanoCeldaPx = (int) (42 * densidad);
        int marginPx = (int) (2 * densidad);

        for (int i = 0; i < offsetLunes; i++) {
            View empty = new View(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = tamanoCeldaPx;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(marginPx, marginPx, marginPx, marginPx);
            empty.setLayoutParams(params);
            gridCalendario.addView(empty);
        }

        for (int d = 1; d <= maxDiasMes; d++) {
            final int diaNum = d;
            String fechaKey = String.format(Locale.getDefault(), "%04d-%02d-%02d", anioActual, mesActualIndex + 1, diaNum);
            boolean tieneRegistro = registrosPorFecha.containsKey(fechaKey);
            JSONObject regDia = tieneRegistro ? registrosPorFecha.get(fechaKey) : null;

            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(0, (int)(4 * densidad), 0, (int)(4 * densidad));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = tamanoCeldaPx;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(marginPx, marginPx, marginPx, marginPx);
            cell.setLayoutParams(params);

            TextView tvDia = new TextView(this);
            tvDia.setText(String.valueOf(diaNum));
            tvDia.setTextSize(13);
            tvDia.setGravity(Gravity.CENTER);
            tvDia.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

            View dot = new View(this);
            int dotSize = (int) (6 * densidad);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dotSize, dotSize);
            dotParams.topMargin = (int) (3 * densidad);
            dot.setLayoutParams(dotParams);
            dot.setBackgroundResource(R.drawable.bg_calendar_dot);

            if (tieneRegistro && regDia != null) {
                String calStr = regDia.optString("calidad", "Buena");
                if (calStr.contains("Muy buena") || calStr.contains("Buena")) {
                    dot.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.accent_mint));
                } else if (calStr.contains("Regular")) {
                    dot.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.warning));
                } else {
                    dot.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.error));
                }
            } else {
                dot.setVisibility(View.INVISIBLE);
            }

            cell.addView(tvDia);
            cell.addView(dot);

            dayViews.put(diaNum, cell);
            dayTextViews.put(diaNum, tvDia);

            cell.setOnClickListener(v -> seleccionarDia(diaNum, regDia));

            gridCalendario.addView(cell);
        }

        int diaASeleccionar = -1;
        JSONObject regASeleccionar = null;

        if (diaSeleccionado > 0 && diaSeleccionado <= maxDiasMes) {
            diaASeleccionar = diaSeleccionado;
            String k = String.format(Locale.getDefault(), "%04d-%02d-%02d", anioActual, mesActualIndex + 1, diaASeleccionar);
            regASeleccionar = registrosPorFecha.get(k);
        } else if (!registrosPorFecha.isEmpty()) {
            int maxDia = -1;
            for (String fecha : registrosPorFecha.keySet()) {
                try {
                    int d = Integer.parseInt(fecha.substring(8));
                    if (d > maxDia) maxDia = d;
                } catch (Exception ignored) {}
            }
            if (maxDia > 0) {
                diaASeleccionar = maxDia;
                String k = String.format(Locale.getDefault(), "%04d-%02d-%02d", anioActual, mesActualIndex + 1, diaASeleccionar);
                regASeleccionar = registrosPorFecha.get(k);
            }
        }

        if (diaASeleccionar == -1) {
            Calendar now = Calendar.getInstance();
            if (now.get(Calendar.YEAR) == anioActual && now.get(Calendar.MONTH) == mesActualIndex) {
                diaASeleccionar = Math.min(now.get(Calendar.DAY_OF_MONTH), maxDiasMes);
            } else {
                diaASeleccionar = 1;
            }
            String k = String.format(Locale.getDefault(), "%04d-%02d-%02d", anioActual, mesActualIndex + 1, diaASeleccionar);
            regASeleccionar = registrosPorFecha.get(k);
        }

        seleccionarDia(diaASeleccionar, regASeleccionar);
    }

    private void seleccionarDia(int diaNum, JSONObject reg) {

        if (diaSeleccionado != -1 && dayViews.containsKey(diaSeleccionado)) {
            LinearLayout prevCell = dayViews.get(diaSeleccionado);
            TextView prevTv = dayTextViews.get(diaSeleccionado);
            if (prevCell != null) {
                prevCell.setBackground(null);
            }
            if (prevTv != null) {
                prevTv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                prevTv.setTypeface(null, Typeface.NORMAL);
            }
        }

        diaSeleccionado = diaNum;

        String fechaKey = String.format(Locale.getDefault(), "%04d-%02d-%02d", anioActual, mesActualIndex + 1, diaNum);
        if (registrosPorFecha.containsKey(fechaKey)) {
            reg = registrosPorFecha.get(fechaKey);
        }

        if (dayViews.containsKey(diaNum)) {
            LinearLayout curCell = dayViews.get(diaNum);
            TextView curTv = dayTextViews.get(diaNum);
            if (curCell != null) {
                curCell.setBackgroundResource(R.drawable.bg_calendar_selected_day);
                curCell.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
            }
            if (curTv != null) {
                curTv.setTextColor(Color.WHITE);
                curTv.setTypeface(null, Typeface.BOLD);
            }
        }

        mostrarDetalleDia(diaNum, reg);
    }

    private void mostrarDetalleDia(int diaNum, JSONObject reg) {
        if (cardDetalleDia == null) return;
        cardDetalleDia.setVisibility(View.VISIBLE);

        String fechaTitulo = diaNum + " de " + mesesNombres[mesActualIndex];
        if (tvDetalleFecha != null) {
            tvDetalleFecha.setText("Noche del " + fechaTitulo);
        }

        if (reg != null) {
            String calidad = reg.optString("calidad", "Regular");
            int durMin = reg.optInt("duracion_minutos", 0);
            String pantalla = reg.optString("pantallas", "Menos de 30 min");

            if (tvDetalleCalidad != null) {
                tvDetalleCalidad.setText(getEmojiForCalidad(calidad) + " " + calidad);
                if (calidad.contains("Muy buena") || calidad.contains("Buena")) {
                    tvDetalleCalidad.setTextColor(ContextCompat.getColor(this, R.color.accent_mint));
                } else if (calidad.contains("Regular")) {
                    tvDetalleCalidad.setTextColor(ContextCompat.getColor(this, R.color.warning));
                } else {
                    tvDetalleCalidad.setTextColor(ContextCompat.getColor(this, R.color.error));
                }
            }

            if (tvDetalleDuracion != null) {
                int h = durMin / 60;
                int m = durMin % 60;
                tvDetalleDuracion.setText("⏱️ Duración: " + h + "h " + String.format(Locale.getDefault(), "%02dm", m));
            }

            if (tvDetallePantalla != null) {
                tvDetallePantalla.setText("📱 Pantalla: " + pantalla);
            }
        } else {
            if (tvDetalleCalidad != null) {
                tvDetalleCalidad.setText("Sin registro");
                tvDetalleCalidad.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            }
            if (tvDetalleDuracion != null) {
                tvDetalleDuracion.setText("No se registró monitoreo en esta noche");
            }
            if (tvDetallePantalla != null) {
                tvDetallePantalla.setText("Toca aquí para registrar tu descanso 📝");
            }
        }
    }

    private String getEmojiForCalidad(String calidad) {
        if (calidad == null) return "😐";
        switch (calidad) {
            case "Muy buena": return "🤩";
            case "Buena": return "🙂";
            case "Mala": return "🙁";
            case "Muy mala": return "😫";
            default: return "😐";
        }
    }

    private void volverAInicio() {
        Intent intent = new Intent(this, ResumenDescansoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
