package com.example.sleeptrackapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class RecomendacionesActivity extends AppCompatActivity {

    View ivVolver;
    View cardConsejoPersonalizado, cardAlertaPreventiva, cardLogroSemanal;
    TextView tvConsejoPersTitulo, tvConsejoPersResumen, tvConsejoPersDesc;
    TextView tvAlertaDesc, tvLogroDesc;

    View cardRecPantallas1, cardRecPantallas2;
    View cardRecHorarios1, cardRecHorarios2;
    View cardRecEstres1, cardRecEstres2;
    View cardRecCafeina1, cardRecCafeina2;

    TextView chipTodos, chipHorarios, chipPantallas, chipEstres, chipCafeina;
    private TextView[] chips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hu19_hu20_recomendaciones);

        ivVolver = findViewById(R.id.iv_hu19_volver);
        if (ivVolver != null) {
            ivVolver.setOnClickListener(v -> volverAInicio());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                volverAInicio();
            }
        });

        cardConsejoPersonalizado = findViewById(R.id.card_consejo_personalizado);
        tvConsejoPersTitulo = findViewById(R.id.tv_consejo_pers_titulo);
        tvConsejoPersResumen = findViewById(R.id.tv_consejo_pers_resumen);
        tvConsejoPersDesc = findViewById(R.id.tv_consejo_pers_desc);

        cardAlertaPreventiva = findViewById(R.id.card_alerta_preventiva);
        cardLogroSemanal = findViewById(R.id.card_logro_semanal);
        tvAlertaDesc = findViewById(R.id.tv_hu20_alerta_desc);
        tvLogroDesc = findViewById(R.id.tv_hu20_logro_desc);

        cardRecPantallas1 = findViewById(R.id.card_rec_pantallas_1);
        cardRecPantallas2 = findViewById(R.id.card_rec_pantallas_2);
        cardRecHorarios1 = findViewById(R.id.card_rec_horarios_1);
        cardRecHorarios2 = findViewById(R.id.card_rec_horarios_2);
        cardRecEstres1 = findViewById(R.id.card_rec_estres_1);
        cardRecEstres2 = findViewById(R.id.card_rec_estres_2);
        cardRecCafeina1 = findViewById(R.id.card_rec_cafeina_1);
        cardRecCafeina2 = findViewById(R.id.card_rec_cafeina_2);

        chipTodos = findViewById(R.id.chip_cat_todos);
        chipHorarios = findViewById(R.id.chip_cat_horarios);
        chipPantallas = findViewById(R.id.chip_cat_pantallas);
        chipEstres = findViewById(R.id.chip_cat_estres);
        chipCafeina = findViewById(R.id.chip_cat_cafeina);

        chips = new TextView[]{chipTodos, chipHorarios, chipPantallas, chipEstres, chipCafeina};

        configurarFiltros();
        adaptarConsejosSegunUso();

        BottomNavHelper.setup(this, R.id.nav_consejos);
    }

    private void configurarFiltros() {
        if (chipTodos != null) chipTodos.setOnClickListener(v -> filtrarCategoria(0));
        if (chipHorarios != null) chipHorarios.setOnClickListener(v -> filtrarCategoria(1));
        if (chipPantallas != null) chipPantallas.setOnClickListener(v -> filtrarCategoria(2));
        if (chipEstres != null) chipEstres.setOnClickListener(v -> filtrarCategoria(3));
        if (chipCafeina != null) chipCafeina.setOnClickListener(v -> filtrarCategoria(4));
    }

    private void filtrarCategoria(int categoriaIndex) {

        for (int i = 0; i < chips.length; i++) {
            if (chips[i] != null) {
                if (i == categoriaIndex) {
                    chips[i].setBackgroundResource(R.drawable.bg_option_selected);
                    chips[i].setTextColor(getColor(R.color.primary));
                } else {
                    chips[i].setBackgroundResource(R.drawable.bg_option_unselected);
                    chips[i].setTextColor(getColor(R.color.text_muted));
                }
            }
        }

        boolean showAll = (categoriaIndex == 0);
        boolean showHorarios = (showAll || categoriaIndex == 1);
        boolean showPantallas = (showAll || categoriaIndex == 2);
        boolean showEstres = (showAll || categoriaIndex == 3);
        boolean showCafeina = (showAll || categoriaIndex == 4);

        setVis(cardRecHorarios1, showHorarios);
        setVis(cardRecHorarios2, showHorarios);
        setVis(cardRecPantallas1, showPantallas);
        setVis(cardRecPantallas2, showPantallas);
        setVis(cardRecEstres1, showEstres);
        setVis(cardRecEstres2, showEstres);
        setVis(cardRecCafeina1, showCafeina);
        setVis(cardRecCafeina2, showCafeina);

        if (cardConsejoPersonalizado != null) {
            cardConsejoPersonalizado.setVisibility((showAll || showPantallas) ? View.VISIBLE : View.GONE);
        }
    }

    private void setVis(View view, boolean visible) {
        if (view != null) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void adaptarConsejosSegunUso() {
        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        int interacciones = prefs.getInt("interacciones_pantalla", 0);
        int calidadPct = prefs.getInt("ultima_calidad_porcentaje", 85);

        if (interacciones > 0) {
            if (tvConsejoPersTitulo != null) tvConsejoPersTitulo.setText("Detectamos " + interacciones + " encendidos de pantalla anoche");
            if (tvConsejoPersResumen != null) tvConsejoPersResumen.setText("⚡ Resumen: Desactiva notificaciones y pon el móvil en 'No Molestar' para evitar microdespertares.");
            if (tvConsejoPersDesc != null) tvConsejoPersDesc.setText("Interactuar con el smartphone a mitad de la noche rompe la arquitectura del sueño REM. Pon el teléfono a más de 1 metro de distancia.");
        } else {
            if (tvConsejoPersTitulo != null) tvConsejoPersTitulo.setText("¡Excelente higiene digital nocturna!");
            if (tvConsejoPersResumen != null) tvConsejoPersResumen.setText("⚡ Resumen: Mantuviste tu celular sin interacciones nocturnas, protegiendo tu sueño profundo.");
            if (tvConsejoPersDesc != null) tvConsejoPersDesc.setText("Continúa dejando tu móvil lejos de la cama para consolidar tu ciclo de descanso.");
        }

        if (calidadPct < 65) {
            if (cardAlertaPreventiva != null) cardAlertaPreventiva.setVisibility(View.VISIBLE);
            if (cardLogroSemanal != null) cardLogroSemanal.setVisibility(View.GONE);
        } else {
            if (cardAlertaPreventiva != null) cardAlertaPreventiva.setVisibility(View.GONE);
            if (cardLogroSemanal != null) cardLogroSemanal.setVisibility(View.VISIBLE);
        }

        String correo = prefs.getString("correo_usuario", "");
        if (correo.isEmpty()) return;

        String url = "http://192.168.3.225/SLEEPTRACKAPP/api/obtener_resumen.php";
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("correo", correo);
        } catch (Exception ignored) {}

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    try {
                        if (response.getString("status").equals("success") && response.has("data")) {
                            JSONObject d = response.getJSONObject("data");
                            String pan = d.optString("pantallas", "");
                            String est = d.optString("estres", "");
                            String caf = d.optString("cafeina", "");

                            if (pan.contains("Más de") || pan.contains("60")) {
                                if (tvConsejoPersTitulo != null) tvConsejoPersTitulo.setText("Alto uso de pantallas antes de dormir");
                                if (tvConsejoPersResumen != null) tvConsejoPersResumen.setText("⚡ Resumen: Corta el uso de pantallas 45 min antes de acostarte.");
                            } else if (caf.contains("más de una") || caf.contains("2")) {
                                if (tvConsejoPersTitulo != null) tvConsejoPersTitulo.setText("Consumo notable de cafeína reportado");
                                if (tvConsejoPersResumen != null) tvConsejoPersResumen.setText("⚡ Resumen: No bebas café después de las 3:00 PM.");
                            }
                        }
                    } catch (Exception ignored) {}
                },
                error -> {}
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void volverAInicio() {
        android.content.Intent intent = new android.content.Intent(this, ResumenDescansoActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
