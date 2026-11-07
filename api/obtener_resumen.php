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

    // Obtener configuración del usuario
    $meta_horas = "8 horas";
    $hora_dormir = "23:00";
    $hora_despertar = "07:00";

    $stmtUser = $conn->prepare("SELECT hora_dormir, hora_despertar, meta_horas FROM cuentas WHERE correo = ?");
    if ($stmtUser) {
        $stmtUser->bind_param("s", $correo);
        $stmtUser->execute();
        $userRes = $stmtUser->get_result();
        if ($userRow = $userRes->fetch_assoc()) {
            if (!empty($userRow['meta_horas'])) $meta_horas = $userRow['meta_horas'];
            if (!empty($userRow['hora_dormir'])) $hora_dormir = $userRow['hora_dormir'];
            if (!empty($userRow['hora_despertar'])) $hora_despertar = $userRow['hora_despertar'];
        }
        $stmtUser->close();
    }

    $stmt = $conn->prepare("SELECT * FROM registros_diarios WHERE correo = ? ORDER BY fecha DESC, id DESC LIMIT 1");
    if ($stmt) {
        $stmt->bind_param("s", $correo);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result && $result->num_rows > 0) {
            $row = $result->fetch_assoc();
            $row['target_meta_horas'] = $meta_horas;
            $row['target_hora_dormir'] = $hora_dormir;
            $row['target_hora_despertar'] = $hora_despertar;
            echo json_encode(["status" => "success", "data" => $row]);
        } else {
            echo json_encode([
                "status" => "empty", 
                "message" => "No hay registros cargados aún.",
                "data" => [
                    "target_meta_horas" => $meta_horas,
                    "target_hora_dormir" => $hora_dormir,
                    "target_hora_despertar" => $hora_despertar
                ]
            ]);
        }
        $stmt->close();
    } else {
        echo json_encode(["status" => "error", "message" => "Error al consultar: " . $conn->error]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Correo no proporcionado."]);
}

$conn->close();
?>
