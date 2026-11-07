package com.example.sleeptrackapp;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class CustomToast {

    public static void show(Activity activity, String message, boolean isError) {
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView == null) return;

        // Si el activity tiene un decor view diferente (como cuando hay un AlertDialog encima), 
        // intentar anclarnos a la vista más alta disponible
        View decorView = activity.getWindow().getDecorView();
        
        Snackbar snackbar = Snackbar.make(decorView, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        
        // Estilo tipo Isla Dinámica / Toast Moderno de iOS
        snackbarView.setBackgroundResource(R.drawable.bg_custom_toast);
        
        // Ajustar posición para que parezca una notificación flotante (arriba o abajo)
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        // Bajarlo un poco para que no se pegue al marco superior
        params.setMargins(20, 80, 20, 0); 
        snackbarView.setLayoutParams(params);
        snackbarView.setElevation(24f);

        // Estilizar el texto
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (textView != null) {
            textView.setTextColor(Color.WHITE);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextSize(14f);
            // Si es un mensaje de error, podemos añadir un prefijo visual o cambiar su estilo si quisieras
            if (isError) {
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error_outline, 0, 0, 0);
                textView.setCompoundDrawablePadding(16);
            } else {
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
                textView.setCompoundDrawablePadding(16);
            }
        }

        snackbar.show();
    }
}