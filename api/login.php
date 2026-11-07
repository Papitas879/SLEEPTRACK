<?php
header('Content-Type: application/json; charset=UTF-8');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

include 'db_connect.php';

$json = file_get_contents('php://input');
$data = json_decode($json, true);

if (empty($data['correo']) || empty($data['password'])) {
    echo json_encode(["status" => "error", "message" => "Por favor, ingresa tu correo y contraseña."]);
    exit();
}

$correo = trim($data['correo']);
$password_input = $data['password'];

$stmt = $conn->prepare("SELECT id, nombres, password, fecha_nacimiento, universidad, carrera, hora_dormir, hora_despertar, meta_horas, sensores_aceptados FROM cuentas WHERE correo = ?");
if ($stmt) {
    $stmt->bind_param("s", $correo);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result && $result->num_rows > 0) {
        $row = $result->fetch_assoc();
        if (password_verify($password_input, $row['password'])) {
            unset($row['password']);
            echo json_encode(["status" => "success", "message" => "Inicio de sesión exitoso.", "user" => $row]);
        } else {
            echo json_encode(["status" => "error", "message" => "Contraseña incorrecta. Inténtalo de nuevo."]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "No existe una cuenta registrada con este correo."]);
    }
    $stmt->close();
} else {
    echo json_encode(["status" => "error", "message" => "Error de base de datos: " . $conn->error]);
}

$conn->close();
?>
