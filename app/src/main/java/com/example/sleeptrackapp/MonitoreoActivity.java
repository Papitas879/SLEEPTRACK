package com.example.sleeptrackapp;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

public class MonitoreoActivity extends AppCompatActivity implements SensorEventListener {

    View btnVolver;
    View badgeDot;
    View vGlowHalo;
    ObjectAnimator pulseAnimator;
    TextView tvBadgeTexto, tvRelojGrande, tvTiempoTranscurrido, tvLblInicio, tvEstadoSubtexto;
    TextView tvSensorAccelEstado, tvSensorPantallaEstado;
    Button btnAccion;

    private boolean isMonitoring = false;
    private long startTimeMillis = 0;
    private Handler timeHandler;
    private Runnable timerRunnable;

    // Acelerómetro nativo
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int contadorMovimientos = 0;
    private long ultimoMovimientoTime = 0;

    // Monitoreo de encendidos de pantalla (Interacciones nocturnas funcionales)
    private int contadorPantallas = 0;
    private BroadcastReceiver screenReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hu07_hu08_monitoreo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnVolver = findViewById(R.id.btn_hu05_volver);
        badgeDot = findViewById(R.id.v_hu05_badge_dot);
        tvBadgeTexto = findViewById(R.id.tv_hu05_badge_texto);
        tvRelojGrande = findViewById(R.id.tv_hu05_reloj_grande);
        tvTiempoTranscurrido = findViewById(R.id.tv_hu05_tiempo_transcurrido);
        tvEstadoSubtexto = findViewById(R.id.tv_hu05_estado_subtexto);
        tvLblInicio = findViewById(R.id.tv_hu05_lbl_inicio);
        btnAccion = findViewById(R.id.btn_hu05_accion);
        vGlowHalo = findViewById(R.id.v_hu05_glow_halo);
        tvSensorAccelEstado = findViewById(R.id.tv_hu05_sensor_accel_estado);
        tvSensorPantallaEstado = findViewById(R.id.tv_hu05_sensor_pantalla_estado);

        // Inicializar SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> manejarSalidaMonitoreo());
        }

        configurarEstadoInicial();
        iniciarReloj();

        if (btnAccion != null) {
            btnAccion.setOnClickListener(v -> {
                if (!isMonitoring) {
                    iniciarMonitoreo();
                } else {
                    finalizarMonitoreo();
                }
            });
        }

        // Configurar receptor de encendidos de pantalla
        screenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isMonitoring && (Intent.ACTION_SCREEN_ON.equals(intent.getAction()) || Intent.ACTION_USER_PRESENT.equals(intent.getAction()))) {
                    contadorPantallas++;
                    runOnUiThread(() -> {
                        if (tvSensorPantallaEstado != null) {
                            tvSensorPantallaEstado.setText(contadorPantallas + (contadorPantallas == 1 ? " evento" : " eventos"));
                        }
                    });
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenReceiver, filter);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                manejarSalidaMonitoreo();
            }
        });
    }

    private void configurarEstadoInicial() {
        isMonitoring = false;
        if (btnAccion != null) {
            btnAccion.setText("🌙  Iniciar monitoreo");
            btnAccion.setBackgroundResource(R.drawable.bg_button_primary);
            btnAccion.setTextColor(Color.WHITE);
        }
        if (tvBadgeTexto != null) {
            tvBadgeTexto.setText("En espera");
            tvBadgeTexto.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }
        if (badgeDot != null) {
            badgeDot.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_muted)));
        }

        if (tvSensorAccelEstado != null) {
            tvSensorAccelEstado.setText("Listo");
        }
        if (tvSensorPantallaEstado != null) {
            tvSensorPantallaEstado.setText("0 eventos");
        }

        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        String hDormir = prefs.getString("hora_dormir", "23:00");
        String hDespertar = prefs.getString("hora_despertar", "07:00");
        String durTexto = prefs.getString("duracion_calculada_texto", "8 horas de descanso");

        if (tvLblInicio != null) {
            tvLblInicio.setText("Objetivo: " + hDormir + " a " + hDespertar + " (" + durTexto + ")");
        }
        if (tvTiempoTranscurrido != null) {
            tvTiempoTranscurrido.setText("00h 00m 00s");
        }
        if (tvEstadoSubtexto != null) {
            tvEstadoSubtexto.setText("Presiona iniciar para comenzar a medir tu descanso");
        }
    }

    private void iniciarMonitoreo() {
        isMonitoring = true;
        startTimeMillis = System.currentTimeMillis();
        contadorMovimientos = 0;
        contadorPantallas = 0;

        // Registrar Acelerómetro
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        String hDespertar = prefs.getString("hora_despertar", "07:00");

        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String horaInicio = sdfHora.format(new Date(startTimeMillis));

        if (tvLblInicio != null) {
            tvLblInicio.setText("Inicio: " + horaInicio + " | Despertar obj: " + hDespertar);
        }

        if (tvBadgeTexto != null) {
            tvBadgeTexto.setText("Monitoreando descanso");
            tvBadgeTexto.setTextColor(ContextCompat.getColor(this, R.color.accent_mint));
        }
        if (badgeDot != null) {
            badgeDot.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accent_mint)));
        }

        if (tvSensorAccelEstado != null) {
            tvSensorAccelEstado.setText("0 movs");
        }
        if (tvSensorPantallaEstado != null) {
            tvSensorPantallaEstado.setText("0 eventos");
        }

        if (tvEstadoSubtexto != null) {
            tvEstadoSubtexto.setText("Midiendo tu descanso en tiempo real... ¡Que duermas bien!");
        }

        if (btnAccion != null) {
            btnAccion.setText("⏹️  Finalizar monitoreo");
            btnAccion.setBackgroundResource(R.drawable.bg_button_finish);
            btnAccion.setTextColor(ContextCompat.getColor(this, R.color.error));
        }

        startPulseAnimation();

        // Mantener la pantalla activa en modo descanso para que el acelerómetro no sea suspendido por el sistema
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        CustomToast.show(this, "Monitoreo iniciado. ¡Que descanses!", false);
    }

    private void finalizarMonitoreo() {
        isMonitoring = false;
        long finMillis = System.currentTimeMillis();

        // Liberar flag de pantalla activa
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Desregistrar Acelerómetro
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        stopPulseAnimation();

        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        String correo = prefs.getString("correo_usuario", "");
        int targetMinutos = prefs.getInt("duracion_minutos_totales", 480);

        long duracionMillis = finMillis - startTimeMillis;
        int duracionMinutos = (int) (duracionMillis / (1000 * 60));
        if (duracionMinutos <= 0 && duracionMillis >= 25000) {
            duracionMinutos = 1;
        } else if (duracionMinutos < 0) {
            duracionMinutos = 0;
        }

        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String horaInicio = sdfHora.format(new Date(startTimeMillis));
        String horaFin = sdfHora.format(new Date(finMillis));
        SimpleDateFormat sdfFecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String fechaHoy = sdfFecha.format(new Date(finMillis));

        int calidadPorcentaje = SleepTimeUtils.calcularCalidadPorcentaje(duracionMinutos, targetMinutos, contadorMovimientos);
        int eficienciaPorcentaje = SleepTimeUtils.calcularEficiencia(duracionMinutos, contadorMovimientos);

        // Guardar en SharedPreferences locales
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("ultimo_monitoreo_fin", finMillis);
        editor.putLong("ultimo_monitoreo_inicio", startTimeMillis);
        editor.putInt("movimientos_detectados", contadorMovimientos);
        editor.putInt("interacciones_pantalla", contadorPantallas);
        editor.putInt("ultima_duracion_minutos", duracionMinutos);
        editor.putInt("ultima_calidad_porcentaje", calidadPorcentaje);
        editor.putInt("ultima_eficiencia", eficienciaPorcentaje);
        editor.putString("ultima_hora_inicio", horaInicio);
        editor.putString("ultima_hora_fin", horaFin);
        editor.apply();

        // Enviar a la Base de Datos MySQL XAMPP
        if (!correo.isEmpty()) {
            String url = "http://192.168.3.225/SLEEPTRACKAPP/api/guardar_monitoreo.php";
            JSONObject payload = new JSONObject();
            try {
                payload.put("correo", correo);
                payload.put("fecha", fechaHoy);
                payload.put("duracion_minutos", duracionMinutos);
                payload.put("hora_inicio", horaInicio);
                payload.put("hora_fin", horaFin);
                payload.put("eficiencia", eficienciaPorcentaje);
                payload.put("calidad_porcentaje", calidadPorcentaje);
                payload.put("movimientos", contadorMovimientos);
                payload.put("pantallas", contadorPantallas + " eventos");
            } catch (Exception e) {
                e.printStackTrace();
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload,
                    response -> {},
                    error -> {}
            );
            Volley.newRequestQueue(this).add(request);
        }

        CustomToast.show(this, "Sesión finalizada: " + duracionMinutos + " min, " + contadorMovimientos + " movs", false);

        // Flujo del Wireframe: Monitoreo finalizado -> Registro Diario de Sensaciones -> Resumen del descanso
        Intent intent = new Intent(MonitoreoActivity.this, RegistroDiarioActivity.class);
        intent.putExtra("desde_monitoreo", true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isMonitoring || event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gVector = (float) Math.sqrt(x * x + y * y + z * z);
        float deltaG = Math.abs(gVector - SensorManager.GRAVITY_EARTH);

        // Si se detecta un movimiento durante el sueño (> 2.2 m/s^2)
        if (deltaG > 2.2f) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - ultimoMovimientoTime > 1800) {
                contadorMovimientos++;
                ultimoMovimientoTime = currentTime;

                runOnUiThread(() -> {
                    if (tvSensorAccelEstado != null) {
                        tvSensorAccelEstado.setText(contadorMovimientos + (contadorMovimientos == 1 ? " mov" : " movs"));
                    }
                });
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void iniciarReloj() {
        timeHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                if (tvRelojGrande != null) {
                    tvRelojGrande.setText(sdf.format(new Date()));
                }

                if (isMonitoring && startTimeMillis > 0 && tvTiempoTranscurrido != null) {
                    long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
                    long horas = elapsedMillis / (1000 * 60 * 60);
                    long minutos = (elapsedMillis / (1000 * 60)) % 60;
                    long segundos = (elapsedMillis / 1000) % 60;
                    tvTiempoTranscurrido.setText(String.format(Locale.getDefault(), "%02dh %02dm %02ds", horas, minutos, segundos));
                }

                timeHandler.postDelayed(this, 1000);
            }
        };
        timeHandler.post(timerRunnable);
    }

    private void startPulseAnimation() {
        if (vGlowHalo == null) return;

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.25f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.25f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.22f, 0.5f);

        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(vGlowHalo, scaleX, scaleY, alpha);
        pulseAnimator.setDuration(2200);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.start();
    }

    private void stopPulseAnimation() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }
        if (vGlowHalo != null) {
            vGlowHalo.setScaleX(1.0f);
            vGlowHalo.setScaleY(1.0f);
            vGlowHalo.setAlpha(0.22f);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPulseAnimation();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        if (screenReceiver != null) {
            try {
                unregisterReceiver(screenReceiver);
            } catch (Exception ignored) {}
        }
        if (timeHandler != null && timerRunnable != null) {
            timeHandler.removeCallbacks(timerRunnable);
        }
    }

    private void manejarSalidaMonitoreo() {
        if (isMonitoring) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("¿Cancelar monitoreo?")
                    .setMessage("La sesión de monitoreo está en curso. Si sales ahora, no se registrarán los datos de esta noche.")
                    .setPositiveButton("Sí, salir", (dialog, which) -> {
                        detenerMonitoreoSinGuardar();
                        finish();
                    })
                    .setNegativeButton("Continuar monitoreando", null)
                    .show();
        } else {
            finish();
        }
    }

    private void detenerMonitoreoSinGuardar() {
        isMonitoring = false;
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        stopPulseAnimation();
    }
}
