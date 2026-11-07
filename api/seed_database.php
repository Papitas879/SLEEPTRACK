<?php
header('Content-Type: text/html; charset=UTF-8');
include 'db_connect.php';

$sqlFile = dirname(__DIR__) . '/seed_data.sql';

if (!file_exists($sqlFile)) {
    die("<h2 style='color:red;'>Error: No se encontró el archivo seed_data.sql</h2>");
}

$sqlContent = file_get_contents($sqlFile);

// Ejecutar sentencias SQL
if ($conn->multi_query($sqlContent)) {
    do {
        // Almacenar primer juego de resultados
        if ($result = $conn->store_result()) {
            $result->free();
        }
    } while ($conn->more_results() && $conn->next_result());
} else {
    die("<h2 style='color:red;'>Error ejecutando SQL: " . $conn->error . "</h2>");
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Base de Datos Poblada - SleepTrack</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0f172a; color: #f8fafc; padding: 30px; }
        .container { max-width: 850px; margin: auto; background: #1e293b; border-radius: 16px; padding: 25px; box-shadow: 0 10px 25px rgba(0,0,0,0.4); }
        h1 { color: #38bdf8; margin-top: 0; }
        .badge { background: #10b981; color: white; padding: 4px 10px; border-radius: 999px; font-size: 13px; font-weight: bold; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #334155; }
        th { background: #0f172a; color: #94a3b8; font-weight: 600; }
        tr:hover { background: #334155; }
        .pass { font-family: monospace; background: #0f172a; padding: 3px 6px; border-radius: 4px; color: #fbbf24; }
        .note { margin-top: 20px; padding: 15px; background: rgba(56, 189, 248, 0.1); border-left: 4px solid #38bdf8; border-radius: 4px; font-size: 14px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🌙 SleepTrack - Base de Datos Actualizada</h1>
        <p><span class="badge">ÉXITO</span> Se han insertado <strong>5 cuentas reales</strong> con datos completos de uso para <strong>Septiembre y Octubre 2026</strong> (más de 230 registros de sueño, encuestas diarias y monitoreo con acelerómetro).</p>

        <h3>Cuentas de Acceso para la Demostración:</h3>
        <table>
            <thead>
                <tr>
                    <th>Estudiante</th>
                    <th>Correo</th>
                    <th>Contraseña</th>
                    <th>Fecha Nac.</th>
                    <th>Universidad y Carrera</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><strong>Jose Pablo Lopez Arellano</strong></td>
                    <td><code>jose.lopez@upn.pe</code></td>
                    <td><span class="pass">Sleep2026!</span></td>
                    <td>14/03/2005 (21 años)</td>
                    <td>UPN - Ing. de Sistemas</td>
                </tr>
                <tr>
                    <td><strong>Kevin Anderson Ortiz More</strong></td>
                    <td><code>kevin.ortiz@unmsm.edu.pe</code></td>
                    <td><span class="pass">Sleep2026!</span></td>
                    <td>28/07/2005 (21 años)</td>
                    <td>UNMSM - Medicina Humana</td>
                </tr>
                <tr>
                    <td><strong>Gerson Manuel Soto Principe</strong></td>
                    <td><code>gerson.soto@uni.edu.pe</code></td>
                    <td><span class="pass">Sleep2026!</span></td>
                    <td>19/11/2005 (20 años)</td>
                    <td>UNI - Ing. Industrial</td>
                </tr>
                <tr>
                    <td><strong>Anderson Socimo Tarazona Saenz</strong></td>
                    <td><code>anderson.tarazona@upn.pe</code></td>
                    <td><span class="pass">Sleep2026!</span></td>
                    <td>05/02/2006 (20 años)</td>
                    <td>UPN - Ing. de Software</td>
                </tr>
                <tr>
                    <td><strong>Valeria Nicole Ramos Castro</strong></td>
                    <td><code>valeria.ramos@pucp.edu.pe</code></td>
                    <td><span class="pass">Sleep2026!</span></td>
                    <td>22/09/2005 (21 años)</td>
                    <td>PUCP - Psicología</td>
                </tr>
            </tbody>
        </table>

        <div class="note">
            💡 <strong>Comportamiento realista implementado:</strong><br>
            • Incluye registros para todo <strong>Septiembre y Octubre 2026</strong> con días alternados sin usar (como una persona normal).<br>
            • Simula semanas de exámenes parciales en octubre (con mayor estrés, consumo de cafeína y menos horas de sueño).<br>
            • Muestra fines de semana con horarios extendidos y diferente eficiencia para ver gráficos variados en el Historial Mensual y Análisis Semanal.
        </div>
    </div>
</body>
</html>

