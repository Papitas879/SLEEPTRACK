package com.example.sleeptrackapp;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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

public class PrivacidadDatosActivity extends AppCompatActivity {

    View ivVolver, btnEliminarRegistros;
    TextView tvTitulo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hu23_privacidad_datos);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivVolver = findViewById(R.id.iv_hu23_volver);
        tvTitulo = findViewById(R.id.tv_hu23_titulo);
        btnEliminarRegistros = findViewById(R.id.ll_hu23_btn_eliminar);

        if (ivVolver != null) {
            ivVolver.setOnClickListener(v -> finish());
        }

        if (btnEliminarRegistros != null) {
            btnEliminarRegistros.setOnClickListener(v -> mostrarDialogoEliminar());
        }

        BottomNavHelper.setup(this, R.id.nav_perfil);
    }

    private void mostrarDialogoEliminar() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_hu23_eliminar_registros, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Button btnCancelar = dialogView.findViewById(R.id.btn_hu23_cancelar_eliminacion);
        Button btnConfirmar = dialogView.findViewById(R.id.btn_hu23_confirmar_eliminacion);

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(v -> {
                SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
                String correo = prefs.getString("correo_usuario", "");

                prefs.edit()
                        .remove("ultimo_monitoreo_inicio")
                        .remove("ultimo_monitoreo_fin")
                        .remove("movimientos_detectados")
                        .remove("ultima_duracion_minutos")
                        .remove("ultima_calidad_porcentaje")
                        .remove("ultima_eficiencia")
                        .remove("ultima_hora_inicio")
                        .remove("ultima_hora_fin")
                        .apply();

                if (!correo.isEmpty()) {
                    String url = "http://192.168.3.225/SLEEPTRACKAPP/api/eliminar_datos.php";
                    JSONObject payload = new JSONObject();
                    try {
                        payload.put("correo", correo);
                        payload.put("tipo", "registros");
                    } catch (Exception ignored) {}

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload,
                            response -> {},
                            error -> {}
                    );
                    Volley.newRequestQueue(this).add(request);
                }

                dialog.dismiss();
                CustomToast.show(this, "Registros de sueño eliminados correctamente", false);
            });
        }

        dialog.show();
    }
}
