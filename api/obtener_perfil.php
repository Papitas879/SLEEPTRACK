<?php
header('Content-Type: application/json; charset=UTF-8');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

include 'db_connect.php';

$json = file_get_contents('php://input');
$data = json_decode($json, true);

if (!empty($data['correo'])) {
    $correo = trim($data['correo']);

    $user = [
        "nombres" => "Estudiante",
        "correo" => $correo,
        "carrera" => "Carrera Universitaria",
        "universidad" => "Universidad Nacional",
        "hora_dormir" => "23:00",
        "hora_despertar" => "07:00",
        "meta_horas" => "8 horas"
    ];

    $stmtUser = $conn->prepare("SELECT nombres, correo, carrera, universidad, hora_dormir, hora_despertar, meta_horas FROM cuentas WHERE correo = ?");
    if ($stmtUser) {
        $stmtUser->bind_param("s", $correo);
        $stmtUser->execute();
        $userRes = $stmtUser->get_result();
        if ($userRow = $userRes->fetch_assoc()) {
            $user = array_merge($user, array_filter($userRow, function($val) { return $val !== null && $val !== ''; }));
        }
        $stmtUser->close();
    }

    $total_noches = 0;
    $prom_minutos = 0;
    $prom_eficiencia = 0;
    $prom_calidad = 0;

    $stmtStats = $conn->prepare("SELECT 
            COUNT(*) as total_noches, 
            AVG(CASE WHEN duracion_minutos > 0 THEN duracion_minutos ELSE NULL END) as prom_minutos, 
            AVG(CASE WHEN eficiencia > 0 THEN eficiencia ELSE NULL END) as prom_eficiencia, 
            AVG(CASE WHEN calidad_porcentaje > 0 THEN calidad_porcentaje ELSE NULL END) as prom_calidad 
        FROM registros_diarios WHERE correo = ?");

    if ($stmtStats) {
        $stmtStats->bind_param("s", $correo);
        $stmtStats->execute();
        $resStats = $stmtStats->get_result();
        if ($sRow = $resStats->fetch_assoc()) {
            $total_noches = (int)$sRow['total_noches'];
            $prom_minutos = (int)round((float)$sRow['prom_minutos']);
            $prom_eficiencia = (int)round((float)$sRow['prom_eficiencia']);
            $prom_calidad = (int)round((float)$sRow['prom_calidad']);
        }
        $stmtStats->close();
    }

    $h = floor($prom_minutos / 60);
    $m = $prom_minutos % 60;
    $prom_dur_texto = sprintf("%dh %02dm", $h, $m);

    echo json_encode([
        "status" => "success",
        "user" => $user,
        "stats" => [
            "total_noches" => $total_noches,
            "promedio_minutos" => $prom_minutos,
            "promedio_duracion_texto" => $prom_dur_texto,
            "promedio_eficiencia" => $prom_eficiencia,
            "promedio_calidad" => $prom_calidad
        ]
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Correo no proporcionado."]);
}

$conn->close();
?>

