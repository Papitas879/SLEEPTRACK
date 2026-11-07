<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "sleeptrack";

// 1. Conexión base a MySQL
$conn = new mysqli($servername, $username, $password);

if ($conn->connect_error) {
    header('Content-Type: application/json; charset=UTF-8');
    echo json_encode(["status" => "error", "message" => "Error de conexión a MySQL: " . $conn->connect_error]);
    exit();
}

// 2. Crear base de datos sleeptrack
$conn->query("CREATE DATABASE IF NOT EXISTS $dbname CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
$conn->select_db($dbname);
$conn->set_charset("utf8mb4");

// 3. Tabla: cuentas (Usuarios)
$conn->query("CREATE TABLE IF NOT EXISTS cuentas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    fecha_nacimiento VARCHAR(50),
    universidad VARCHAR(100),
    carrera VARCHAR(100),
    hora_dormir VARCHAR(10) DEFAULT '23:00',
    hora_despertar VARCHAR(10) DEFAULT '07:00',
    meta_horas VARCHAR(50) DEFAULT '8 horas',
    sensores_aceptados TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_correo (correo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

// 4. Tabla: configuracion_descanso (Configurar descanso)
$conn->query("CREATE TABLE IF NOT EXISTS configuracion_descanso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    correo VARCHAR(100) NOT NULL,
    hora_dormir VARCHAR(10) DEFAULT '23:00',
    hora_despertar VARCHAR(10) DEFAULT '07:00',
    meta_horas VARCHAR(50) DEFAULT '8 horas',
    recordatorios_activos TINYINT(1) DEFAULT 1,
    sensores_aceptados TINYINT(1) DEFAULT 1,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_config_correo (correo),
    CONSTRAINT fk_config_cuentas FOREIGN KEY (correo) REFERENCES cuentas(correo) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

// 5. Tabla: registros_diarios (Consolidado de sueño para Dashboard e Historial)
$conn->query("CREATE TABLE IF NOT EXISTS registros_diarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    correo VARCHAR(100) NOT NULL,
    calidad VARCHAR(50) DEFAULT 'Regular',
    energia VARCHAR(50) DEFAULT 'Media',
    interrupciones VARCHAR(50) DEFAULT '0',
    estres VARCHAR(50) DEFAULT 'Bajo',
    cafeina VARCHAR(50) DEFAULT 'No consumí',
    pantallas VARCHAR(50) DEFAULT 'Menos de 30 minutos',
    duracion_minutos INT DEFAULT 0,
    hora_inicio VARCHAR(10) DEFAULT '',
    hora_fin VARCHAR(10) DEFAULT '',
    eficiencia INT DEFAULT 0,
    calidad_porcentaje INT DEFAULT 0,
    movimientos INT DEFAULT 0,
    fecha DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_registro (correo, fecha),
    INDEX idx_correo_fecha (correo, fecha),
    CONSTRAINT fk_registros_cuentas FOREIGN KEY (correo) REFERENCES cuentas(correo) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

// 6. Tabla: monitoreo_sueno (Acelerómetro y duración de sesión nocturna)
$conn->query("CREATE TABLE IF NOT EXISTS monitoreo_sueno (
    id INT AUTO_INCREMENT PRIMARY KEY,
    correo VARCHAR(100) NOT NULL,
    id_registro INT DEFAULT NULL,
    fecha DATE NOT NULL,
    hora_inicio VARCHAR(10) DEFAULT '',
    hora_fin VARCHAR(10) DEFAULT '',
    duracion_minutos INT DEFAULT 0,
    movimientos INT DEFAULT 0,
    eventos_pantalla VARCHAR(50) DEFAULT '0 eventos',
    eficiencia INT DEFAULT 0,
    calidad_porcentaje INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_mon_correo_fecha (correo, fecha),
    CONSTRAINT fk_monitoreo_cuentas FOREIGN KEY (correo) REFERENCES cuentas(correo) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_monitoreo_registros FOREIGN KEY (id_registro) REFERENCES registros_diarios(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

// 7. Tabla: encuestas_diarias (Factores y emojis de cómo durmió)
$conn->query("CREATE TABLE IF NOT EXISTS encuestas_diarias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    correo VARCHAR(100) NOT NULL,
    id_registro INT DEFAULT NULL,
    fecha DATE NOT NULL,
    calidad_percibida VARCHAR(50) DEFAULT 'Regular',
    nivel_energia VARCHAR(50) DEFAULT 'Media',
    interrupciones VARCHAR(50) DEFAULT '0',
    nivel_estres VARCHAR(50) DEFAULT 'Bajo',
    consumo_cafeina VARCHAR(50) DEFAULT 'No consumí',
    exposicion_pantallas VARCHAR(50) DEFAULT 'Menos de 30 minutos',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_enc_correo_fecha (correo, fecha),
    CONSTRAINT fk_encuestas_cuentas FOREIGN KEY (correo) REFERENCES cuentas(correo) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_encuestas_registros FOREIGN KEY (id_registro) REFERENCES registros_diarios(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

// 8. Tabla: recomendaciones (Consejos de descanso por categorías del app)
$conn->query("CREATE TABLE IF NOT EXISTS recomendaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    categoria VARCHAR(50) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    resumen VARCHAR(255) NOT NULL,
    descripcion TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

// Función auxiliar para agregar columnas si no existen (Migración transparente)
function agregarColumnaSiNoExiste($conn, $tabla, $columna, $definicion) {
    $check = $conn->query("SHOW COLUMNS FROM `$tabla` LIKE '$columna'");
    if ($check && $check->num_rows == 0) {
        $conn->query("ALTER TABLE `$tabla` ADD COLUMN `$columna` $definicion");
    }
}

// Asegurar columnas nuevas en registros_diarios si la tabla ya existía
agregarColumnaSiNoExiste($conn, 'registros_diarios', 'duracion_minutos', 'INT DEFAULT 0');
agregarColumnaSiNoExiste($conn, 'registros_diarios', 'hora_inicio', 'VARCHAR(10) DEFAULT ""');
agregarColumnaSiNoExiste($conn, 'registros_diarios', 'hora_fin', 'VARCHAR(10) DEFAULT ""');
agregarColumnaSiNoExiste($conn, 'registros_diarios', 'eficiencia', 'INT DEFAULT 0');
agregarColumnaSiNoExiste($conn, 'registros_diarios', 'calidad_porcentaje', 'INT DEFAULT 0');
agregarColumnaSiNoExiste($conn, 'registros_diarios', 'movimientos', 'INT DEFAULT 0');
agregarColumnaSiNoExiste($conn, 'registros_diarios', 'created_at', 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP');
agregarColumnaSiNoExiste($conn, 'registros_diarios', 'updated_at', 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');

// Asegurar columnas en cuentas
agregarColumnaSiNoExiste($conn, 'cuentas', 'created_at', 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP');
agregarColumnaSiNoExiste($conn, 'cuentas', 'updated_at', 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
?>