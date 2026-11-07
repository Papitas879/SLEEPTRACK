<?php
header('Content-Type: application/json; charset=UTF-8');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

include 'db_connect.php';

$json = file_get_contents('php://input');
$data = json_decode($json, true);

if (!empty($data['correo']) && !empty($data['fecha_nacimiento']) && !empty($data['nueva_password'])) {
    $correo = trim($data['correo']);
    $fecha_nac = trim($data['fecha_nacimiento']);
    $nueva_password = password_hash($data['nueva_password'], PASSWORD_BCRYPT);

    $stmt = $conn->prepare("SELECT id, fecha_nacimiento FROM cuentas WHERE correo = ?");
    if ($stmt) {
        $stmt->bind_param("s", $correo);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result && $result->num_rows > 0) {
            $row = $result->fetch_assoc();

            if ($row['fecha_nacimiento'] === $fecha_nac) {
                $stmtUpdate = $conn->prepare("UPDATE cuentas SET password = ? WHERE correo = ?");
                $stmtUpdate->bind_param("ss", $nueva_password, $correo);
                if ($stmtUpdate->execute()) {
                    echo json_encode(["status" => "success", "message" => "Contraseña actualizada correctamente. Ya puedes iniciar sesión."]);
                } else {
                    echo json_encode(["status" => "error", "message" => "No se pudo actualizar la contraseña."]);
                }
                $stmtUpdate->close();
            } else {
                echo json_encode(["status" => "error", "message" => "La fecha de nacimiento no coincide con nuestros registros."]);
            }
        } else {
            echo json_encode(["status" => "error", "message" => "El correo electrónico no se encuentra registrado."]);
        }
        $stmt->close();
    } else {
        echo json_encode(["status" => "error", "message" => "Error de preparación: " . $conn->error]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Por favor, completa todos los campos requeridos."]);
}

$conn->close();
?>
