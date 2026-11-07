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
    $mes = isset($data['mes']) ? (int)$data['mes'] : 0;
    $anio = isset($data['anio']) ? (int)$data['anio'] : 0;

    if ($mes > 0 && $anio > 0) {
        $stmt = $conn->prepare("SELECT * FROM registros_diarios WHERE correo = ? AND MONTH(fecha) = ? AND YEAR(fecha) = ? ORDER BY fecha ASC");
        $stmt->bind_param("sii", $correo, $mes, $anio);
    } else {
        $stmt = $conn->prepare("SELECT * FROM registros_diarios WHERE correo = ? ORDER BY fecha DESC LIMIT 60");
        $stmt->bind_param("s", $correo);
    }

    if ($stmt) {
        $stmt->execute();
        $result = $stmt->get_result();

        $registros = array();
        if ($result && $result->num_rows > 0) {
            while ($row = $result->fetch_assoc()) {
                $registros[] = $row;
            }
        }

        echo json_encode(["status" => "success", "total" => count($registros), "data" => $registros]);
        $stmt->close();
    } else {
        echo json_encode(["status" => "error", "message" => "Error al consultar historial: " . $conn->error]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Correo no proporcionado."]);
}

$conn->close();
?>
