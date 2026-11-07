package com.example.sleeptrackapp;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavHelper {

    public static void setup(Activity activity, int selectedItemId) {
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) return;

        bottomNavigationView.setSelectedItemId(selectedItemId);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == selectedItemId) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_inicio) {
                intent = new Intent(activity, ResumenDescansoActivity.class);
            } else if (itemId == R.id.nav_historial) {
                intent = new Intent(activity, HistorialMensualActivity.class);
            } else if (itemId == R.id.nav_registrar) {
                intent = new Intent(activity, RegistroDiarioActivity.class);
            } else if (itemId == R.id.nav_consejos) {
                intent = new Intent(activity, RecomendacionesActivity.class);
            } else if (itemId == R.id.nav_perfil) {
                intent = new Intent(activity, PerfilCuentaActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                activity.finish();
                return true;
            }
            return false;
        });
    }
}
