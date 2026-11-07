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
    $tipo = isset($data['tipo']) ? trim($data['tipo']) : "registros"; // "registros" o "cuenta"

    if ($tipo === "cuenta") {
        // Eliminar registros diarios primero (integridad referencial)
        $stmtReg = $conn->prepare("DELETE FROM registros_diarios WHERE correo = ?");
        $stmtReg->bind_param("s", $correo);
        $stmtReg->execute();
        $stmtReg->close();

        // Eliminar cuenta
        $stmtUser = $conn->prepare("DELETE FROM cuentas WHERE correo = ?");
        $stmtUser->bind_param("s", $correo);
        if ($stmtUser->execute()) {
            echo json_encode(["status" => "success", "message" => "Cuenta y todos sus registros eliminados permanentemente."]);
        } else {
            echo json_encode(["status" => "error", "message" => "Error al eliminar la cuenta: " . $stmtUser->error]);
        }
        $stmtUser->close();
    } else {
        // Eliminar solo registros diarios de historial
        $stmtReg = $conn->prepare("DELETE FROM registros_diarios WHERE correo = ?");
        $stmtReg->bind_param("s", $correo);
        if ($stmtReg->execute()) {
            $eliminados = $stmtReg->affected_rows;
            echo json_encode(["status" => "success", "message" => "Se eliminaron $eliminados registros de sueño correctamente."]);
        } else {
            echo json_encode(["status" => "error", "message" => "Error al eliminar registros: " . $stmtReg->error]);
        }
        $stmtReg->close();
    }
} else {
    echo json_encode(["status" => "error", "message" => "Correo no proporcionado."]);
}

$conn->close();
?>

