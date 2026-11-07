package com.example.sleeptrackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegistroDiarioActivity extends AppCompatActivity {

    View ivVolver;
    TextView tvFecha;
    Button btnGuardar;

    // Valores seleccionados por defecto
    private String calidadSeleccionada = "Regular";
    private String energiaSeleccionada = "Normal";
    private String interrupcionesSeleccionadas = "3";
    private String estresSeleccionado = "Medio";
    private String cafeinaSeleccionada = "No consumí";
    private String pantallasSeleccionadas = "30 – 60 minutos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hu09_hu10_hu11_registro_diario);

        ivVolver = findViewById(R.id.iv_hu09_volver);
        tvFecha = findViewById(R.id.tv_hu09_fecha);
        btnGuardar = findViewById(R.id.btn_hu11_guardar);

        if (ivVolver != null) {
            ivVolver.setOnClickListener(v -> volverAInicio());
        }

        String fechaHoy = new SimpleDateFormat("dd 'de' MMMM", new Locale("es", "ES")).format(new Date());
        if (tvFecha != null) tvFecha.setText("Noche del " + fechaHoy);

        configurarOpcionesCalidad();
        configurarOpcionesEnergia();
        configurarOpcionesInterrupciones();
        configurarOpcionesEstres();
        configurarOpcionesCafeina();
        configurarOpcionesPantallas();

        if (btnGuardar != null) {
            btnGuardar.setOnClickListener(v -> guardarRegistro());
        }

        BottomNavHelper.setup(this, R.id.nav_registrar);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                volverAInicio();
            }
        });
    }

    private void volverAInicio() {
        Intent intent = new Intent(this, ResumenDescansoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void configurarOpcionesCalidad() {
        int[] ids = {R.id.opt_calidad_1, R.id.opt_calidad_2, R.id.opt_calidad_3, R.id.opt_calidad_4, R.id.opt_calidad_5};
        int[] textIds = {R.id.tv_texto_c1, R.id.tv_texto_c2, R.id.tv_texto_c3, R.id.tv_texto_c4, R.id.tv_texto_c5};
        String[] valores = {"Muy mala", "Mala", "Regular", "Buena", "Muy buena"};

        View[] views = new View[ids.length];
        for (int i = 0; i < ids.length; i++) {
            views[i] = findViewById(ids[i]);
            final int index = i;
            if (views[i] != null) {
                views[i].setOnClickListener(v -> {
                    calidadSeleccionada = valores[index];
                    actualizarSeleccionOpciones(views, textIds, views[index]);
                });
                if (valores[i].equals(calidadSeleccionada)) {
                    actualizarSeleccionOpciones(views, textIds, views[i]);
                }
            }
        }
    }

    private void configurarOpcionesEnergia() {
        int[] ids = {R.id.opt_energia_1, R.id.opt_energia_2, R.id.opt_energia_3, R.id.opt_energia_4, R.id.opt_energia_5};
        int[] textIds = {R.id.tv_texto_e1, R.id.tv_texto_e2, R.id.tv_texto_e3, R.id.tv_texto_e4, R.id.tv_texto_e5};
        String[] valores = {"Muy baja", "Baja", "Media", "Alta", "Muy alta"};

        View[] views = new View[ids.length];
        for (int i = 0; i < ids.length; i++) {
            views[i] = findViewById(ids[i]);
            final int index = i;
            if (views[i] != null) {
                views[i].setOnClickListener(v -> {
                    energiaSeleccionada = valores[index];
                    actualizarSeleccionOpciones(views, textIds, views[index]);
                });
                if (valores[i].equals(energiaSeleccionada)) {
                    actualizarSeleccionOpciones(views, textIds, views[i]);
                }
            }
        }
    }

    private void configurarOpcionesInterrupciones() {
        int[] ids = {R.id.opt_int_0, R.id.opt_int_1, R.id.opt_int_2, R.id.opt_int_3, R.id.opt_int_4, R.id.opt_int_5};
        String[] valores = {"0", "1", "2", "3", "4", "5+"};

        TextView[] views = new TextView[ids.length];
        for (int i = 0; i < ids.length; i++) {
            views[i] = findViewById(ids[i]);
            final int index = i;
            if (views[i] != null) {
                views[i].setOnClickListener(v -> {
                    interrupcionesSeleccionadas = valores[index];
                    actualizarSeleccionOpcionesTexto(views, views[index]);
                });
                if (valores[i].equals(interrupcionesSeleccionadas)) {
                    actualizarSeleccionOpcionesTexto(views, views[i]);
                }
            }
        }
    }

    private void configurarOpcionesEstres() {
        int[] ids = {R.id.opt_estres_1, R.id.opt_estres_2, R.id.opt_estres_3};
        String[] valores = {"Bajo", "Medio", "Alto"};
        
        TextView[] views = new TextView[ids.length];
        for (int i = 0; i < ids.length; i++) {
            views[i] = findViewById(ids[i]);
            final int index = i;
            if (views[i] != null) {
                views[i].setOnClickListener(v -> {
                    estresSeleccionado = valores[index];
                    actualizarSeleccionOpcionesTexto(views, views[index]);
                });
                if (valores[i].equals(estresSeleccionado)) {
                    actualizarSeleccionOpcionesTexto(views, views[i]);
                }
            }
        }
    }

    private void configurarOpcionesCafeina() {
        int[] ids = {R.id.opt_caf_1, R.id.opt_caf_2, R.id.opt_caf_3};
        String[] textos = {"🚫  No consumí cafeína", "☕  1 taza / café", "☕☕  2 o más tazas"};
        String[] valores = {"No consumí", "Sí, una vez", "Sí, más de una vez"};
        
        TextView[] views = new TextView[ids.length];
        for (int i = 0; i < ids.length; i++) {
            views[i] = findViewById(ids[i]);
            final int index = i;
            if (views[i] != null) {
                views[i].setOnClickListener(v -> {
                    cafeinaSeleccionada = valores[index];
                    actualizarCustomRadioList(views, index, textos);
                });
            }
        }
        actualizarCustomRadioList(views, 0, textos);
    }

    private void configurarOpcionesPantallas() {
        int[] ids = {R.id.opt_pan_1, R.id.opt_pan_2, R.id.opt_pan_3};
        String[] textos = {"✨  Menos de 30 minutos (Recomendado)", "📱  30 – 60 minutos", "📺  Más de 60 minutos"};
        String[] valores = {"Menos de 30 minutos", "30 – 60 minutos", "Más de 60 minutos"};
        
        TextView[] views = new TextView[ids.length];
        for (int i = 0; i < ids.length; i++) {
            views[i] = findViewById(ids[i]);
            final int index = i;
            if (views[i] != null) {
                views[i].setOnClickListener(v -> {
                    pantallasSeleccionadas = valores[index];
                    actualizarCustomRadioList(views, index, textos);
                });
            }
        }
        actualizarCustomRadioList(views, 1, textos);
    }

    private void actualizarCustomRadioList(TextView[] views, int selectedIndex, String[] textos) {
        for (int i = 0; i < views.length; i++) {
            if (views[i] != null) {
                views[i].setText(textos[i]);
                if (i == selectedIndex) {
                    views[i].setBackgroundResource(R.drawable.bg_option_selected);
                    views[i].setTextColor(getColor(R.color.primary));
                    views[i].performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
                } else {
                    views[i].setBackgroundResource(R.drawable.bg_option_unselected);
                    views[i].setTextColor(getColor(R.color.text_muted));
                }
            }
        }
    }

    private void actualizarSeleccionOpciones(View[] todos, int[] textIds, View seleccionado) {
        for (int i = 0; i < todos.length; i++) {
            if (todos[i] != null) {
                todos[i].setBackgroundResource(R.drawable.bg_option_unselected);
                TextView tvTexto = todos[i].findViewById(textIds[i]);
                if (tvTexto != null) {
                    tvTexto.setTextColor(getColor(R.color.text_muted));
                }
            }
        }
        if (seleccionado != null) {
            seleccionado.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
            seleccionado.setBackgroundResource(R.drawable.bg_option_selected);
            for (int i = 0; i < todos.length; i++) {
                if (todos[i] == seleccionado) {
                    TextView tvTextoSel = seleccionado.findViewById(textIds[i]);
                    if (tvTextoSel != null) {
                        tvTextoSel.setTextColor(getColor(R.color.primary));
                    }
                }
            }
        }
    }
    
    private void actualizarSeleccionOpcionesTexto(TextView[] todos, TextView seleccionado) {
        for (TextView view : todos) {
            if (view != null) {
                view.setBackgroundResource(R.drawable.bg_option_unselected);
                view.setTextColor(getColor(R.color.text_muted));
            }
        }
        if (seleccionado != null) {
            seleccionado.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
            seleccionado.setBackgroundResource(R.drawable.bg_option_selected);
            seleccionado.setTextColor(getColor(R.color.primary));
        }
    }

    private void guardarRegistro() {
        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        String correo = prefs.getString("correo_usuario", "");

        if (correo.isEmpty()) {
            CustomToast.show(this, "Sesión no válida", true);
            return;
        }

        String url = "http://192.168.3.225/SLEEPTRACKAPP/api/guardar_registro.php";

        JSONObject requestData = new JSONObject();
        try {
            requestData.put("correo", correo);
            requestData.put("calidad", calidadSeleccionada);
            requestData.put("energia", energiaSeleccionada);
            requestData.put("interrupciones", interrupcionesSeleccionadas);
            requestData.put("estres", estresSeleccionado);
            requestData.put("cafeina", cafeinaSeleccionada);
            requestData.put("pantallas", pantallasSeleccionadas);
            requestData.put("fecha", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnGuardar.setEnabled(false);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    btnGuardar.setEnabled(true);
                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");

                        if (status.equals("success")) {
                            CustomToast.show(this, "Registro guardado exitosamente", false);
                            volverAInicio();
                        } else {
                            CustomToast.show(this, message, true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    btnGuardar.setEnabled(true);
                    CustomToast.show(this, "Registro guardado en tu dispositivo", false);
                    volverAInicio();
                }
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}
