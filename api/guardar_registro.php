<?php
header('Content-Type: application/json; charset=UTF-8');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

include 'db_connect.php';

$json = file_get_contents('php://input');
$data = json_decode($json, true);

if (
    !empty($data['correo']) &&
    !empty($data['calidad']) &&
    !empty($data['energia']) &&
    !empty($data['interrupciones']) &&
    !empty($data['estres']) &&
    !empty($data['cafeina']) &&
    !empty($data['pantallas']) &&
    !empty($data['fecha'])
) {
    $correo = trim($data['correo']);
    $calidad = trim($data['calidad']);
    $energia = trim($data['energia']);
    $interrupciones = trim($data['interrupciones']);
    $estres = trim($data['estres']);
    $cafeina = trim($data['cafeina']);
    $pantallas = trim($data['pantallas']);
    $fecha = trim($data['fecha']);

    $stmt = $conn->prepare("INSERT INTO registros_diarios 
        (correo, calidad, energia, interrupciones, estres, cafeina, pantallas, fecha)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
        calidad = VALUES(calidad), 
        energia = VALUES(energia), 
        interrupciones = VALUES(interrupciones),
        estres = VALUES(estres), 
        cafeina = VALUES(cafeina), 
        pantallas = VALUES(pantallas)");

    if ($stmt) {
        $stmt->bind_param("ssssssss", $correo, $calidad, $energia, $interrupciones, $estres, $cafeina, $pantallas, $fecha);
        if ($stmt->execute()) {
            $registro_id = $stmt->insert_id;
            if ($registro_id == 0) {
                $checkReg = $conn->query("SELECT id FROM registros_diarios WHERE correo = '$correo' AND fecha = '$fecha'");
                if ($checkReg && $rRow = $checkReg->fetch_assoc()) {
                    $registro_id = (int)$rRow['id'];
                }
            }

            // Sincronizar en encuestas_diarias
            $stmtEnc = $conn->prepare("INSERT INTO encuestas_diarias 
                (correo, id_registro, fecha, calidad_percibida, nivel_energia, interrupciones, nivel_estres, consumo_cafeina, exposicion_pantallas)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            if ($stmtEnc) {
                $stmtEnc->bind_param("sisssssss", $correo, $registro_id, $fecha, $calidad, $energia, $interrupciones, $estres, $cafeina, $pantallas);
                $stmtEnc->execute();
                $stmtEnc->close();
            }

            echo json_encode(["status" => "success", "message" => "Registro guardado correctamente."]);
        } else {
            echo json_encode(["status" => "error", "message" => "No se pudo guardar el registro: " . $stmt->error]);
        }
        $stmt->close();
    } else {
        echo json_encode(["status" => "error", "message" => "Error de preparación: " . $conn->error]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Datos incompletos para el registro diario."]);
}

$conn->close();
?>
