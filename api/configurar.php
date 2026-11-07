<?php
header('Content-Type: application/json; charset=UTF-8');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

include 'db_connect.php';

$json = file_get_contents('php://input');
$data = json_decode($json, true);

if (empty($data['correo'])) {
    echo json_encode(["status" => "error", "message" => "Falta identificar al usuario (correo)."]);
    exit();
}

$correo = trim($data['correo']);
$hora_dormir = isset($data['hora_dormir']) ? trim($data['hora_dormir']) : "23:00";
$hora_despertar = isset($data['hora_despertar']) ? trim($data['hora_despertar']) : "07:00";
$meta_horas = isset($data['meta_horas']) ? trim($data['meta_horas']) : "8 horas";
$sensores_aceptados = isset($data['sensores_aceptados']) ? (int)$data['sensores_aceptados'] : 0;

$stmt = $conn->prepare("UPDATE cuentas SET hora_dormir = ?, hora_despertar = ?, meta_horas = ?, sensores_aceptados = ? WHERE correo = ?");
if ($stmt) {
    $stmt->bind_param("sssis", $hora_dormir, $hora_despertar, $meta_horas, $sensores_aceptados, $correo);
    if ($stmt->execute()) {
        // Sincronizar tabla configuracion_descanso
        $stmtConf = $conn->prepare("INSERT INTO configuracion_descanso 
            (correo, hora_dormir, hora_despertar, meta_horas, sensores_aceptados) 
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE 
                hora_dormir = VALUES(hora_dormir),
                hora_despertar = VALUES(hora_despertar),
                meta_horas = VALUES(meta_horas),
                sensores_aceptados = VALUES(sensores_aceptados)");
        if ($stmtConf) {
            $stmtConf->bind_param("ssssi", $correo, $hora_dormir, $hora_despertar, $meta_horas, $sensores_aceptados);
            $stmtConf->execute();
            $stmtConf->close();
        }

        echo json_encode(["status" => "success", "message" => "Configuración guardada correctamente."]);
    } else {
        echo json_encode(["status" => "error", "message" => "Error al guardar configuración: " . $stmt->error]);
    }
    $stmt->close();
} else {
    echo json_encode(["status" => "error", "message" => "Error de preparación: " . $conn->error]);
}

$conn->close();
?>
