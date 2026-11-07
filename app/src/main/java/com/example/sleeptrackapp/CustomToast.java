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

        View decorView = activity.getWindow().getDecorView();

        Snackbar snackbar = Snackbar.make(decorView, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();

        snackbarView.setBackgroundResource(R.drawable.bg_custom_toast);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        params.setMargins(20, 80, 20, 0);
        snackbarView.setLayoutParams(params);
        snackbarView.setElevation(24f);

        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (textView != null) {
            textView.setTextColor(Color.WHITE);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextSize(14f);

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
