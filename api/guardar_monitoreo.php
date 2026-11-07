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
    $fecha = !empty($data['fecha']) ? trim($data['fecha']) : date('Y-m-d');
    $duracion_minutos = isset($data['duracion_minutos']) ? (int)$data['duracion_minutos'] : 0;
    $hora_inicio = !empty($data['hora_inicio']) ? trim($data['hora_inicio']) : '';
    $hora_fin = !empty($data['hora_fin']) ? trim($data['hora_fin']) : '';
    $eficiencia = isset($data['eficiencia']) ? (int)$data['eficiencia'] : 0;
    $calidad_porcentaje = isset($data['calidad_porcentaje']) ? (int)$data['calidad_porcentaje'] : 0;
    $movimientos = isset($data['movimientos']) ? (int)$data['movimientos'] : 0;
    $pantallas = !empty($data['pantallas']) ? trim($data['pantallas']) : '0 eventos';

    // Calcular clasificación textual de calidad si no existe
    $calidad_texto = "Regular";
    if ($calidad_porcentaje >= 85) {
        $calidad_texto = "Muy buena";
    } else if ($calidad_porcentaje >= 70) {
        $calidad_texto = "Buena";
    } else if ($calidad_porcentaje >= 50) {
        $calidad_texto = "Regular";
    } else if ($calidad_porcentaje >= 35) {
        $calidad_texto = "Mala";
    } else {
        $calidad_texto = "Muy mala";
    }

    $stmt = $conn->prepare("INSERT INTO registros_diarios 
        (correo, fecha, duracion_minutos, hora_inicio, hora_fin, eficiencia, calidad_porcentaje, movimientos, calidad, pantallas)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE 
            duracion_minutos = VALUES(duracion_minutos),
            hora_inicio = VALUES(hora_inicio),
            hora_fin = VALUES(hora_fin),
            eficiencia = VALUES(eficiencia),
            calidad_porcentaje = VALUES(calidad_porcentaje),
            movimientos = VALUES(movimientos),
            pantallas = VALUES(pantallas),
            calidad = IF(calidad IS NULL OR calidad = 'Regular', VALUES(calidad), calidad)");

    if ($stmt) {
        $stmt->bind_param("ssissiiiss", $correo, $fecha, $duracion_minutos, $hora_inicio, $hora_fin, $eficiencia, $calidad_porcentaje, $movimientos, $calidad_texto, $pantallas);
        if ($stmt->execute()) {
            $registro_id = $stmt->insert_id;
            if ($registro_id == 0) {
                $checkReg = $conn->query("SELECT id FROM registros_diarios WHERE correo = '$correo' AND fecha = '$fecha'");
                if ($checkReg && $rRow = $checkReg->fetch_assoc()) {
                    $registro_id = (int)$rRow['id'];
                }
            }

            // Sincronizar en monitoreo_sueno
            $stmtMon = $conn->prepare("INSERT INTO monitoreo_sueno 
                (correo, id_registro, fecha, hora_inicio, hora_fin, duracion_minutos, movimientos, eventos_pantalla, eficiencia, calidad_porcentaje)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            if ($stmtMon) {
                $stmtMon->bind_param("sissiisiii", $correo, $registro_id, $fecha, $hora_inicio, $hora_fin, $duracion_minutos, $movimientos, $pantallas, $eficiencia, $calidad_porcentaje);
                $stmtMon->execute();
                $stmtMon->close();
            }

            echo json_encode([
                "status" => "success", 
                "message" => "Sesión de monitoreo guardada correctamente.",
                "data" => [
                    "correo" => $correo,
                    "fecha" => $fecha,
                    "duracion_minutos" => $duracion_minutos,
                    "eficiencia" => $eficiencia,
                    "calidad_porcentaje" => $calidad_porcentaje,
                    "calidad" => $calidad_texto,
                    "pantallas" => $pantallas
                ]
            ]);
        } else {
            echo json_encode(["status" => "error", "message" => "Error al ejecutar: " . $stmt->error]);
        }
        $stmt->close();
    } else {
        echo json_encode(["status" => "error", "message" => "Error de preparación: " . $conn->error]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Correo no proporcionado."]);
}

$conn->close();
?>
