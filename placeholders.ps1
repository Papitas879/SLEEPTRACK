$base_path = "C:\xampp\htdocs\SLEEPTRACKAPP\app\src\main"
$java_dir = "$base_path\java\com\example\sleeptrackapp"
$res_dir = "$base_path\res\layout"

$missing = @(
    "Hu04RecuperarPasswordActivity",
    "Hu08FinalizarMonitoreoActivity",
    "Hu09RegistrarExperienciaActivity",
    "Hu10RegistrarFactoresActivity",
    "Hu11GuardarRegistroActivity",
    "Hu13LineaTiempoActivity",
    "Hu14RecomendacionesResumenActivity",
    "Hu15ResumenSemanalActivity",
    "Hu16HorariosAproximadosActivity",
    "Hu17HistorialMensualActivity",
    "Hu18EstadisticasMensualesActivity",
    "Hu19RecomendacionesCategoriaActivity",
    "Hu21ConsultarPerfilActivity",
    "Hu22CerrarSesionActivity"
)

foreach ($act in $missing) {
    # Generate layout name (e.g., activity_hu04_recuperar_password.xml)
    # Using regex to convert CamelCase to snake_case
    $snake = $act -creplace '([A-Z])', '_$1' | ForEach-Object { $_.ToLower() }
    $layout_name = "activity" + $snake.Replace("_activity", "")

    # Write Java file
    $java_content = @"
package com.example.sleeptrackapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class $act extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.$layout_name);
    }
}
"@
    Set-Content -Path "$java_dir\$act.java" -Value $java_content -Encoding UTF8

    # Write Layout file
    $layout_content = @"
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="$act"
        android:textSize="20sp" />
</LinearLayout>
"@
    Set-Content -Path "$res_dir\$layout_name.xml" -Value $layout_content -Encoding UTF8
}
Write-Host "Placeholders created"
