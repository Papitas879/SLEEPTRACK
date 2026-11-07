CREATE DATABASE IF NOT EXISTS sleeptrack CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE sleeptrack;

-- 1. TABLA: cuentas (Usuarios registrados en Crear Perfil / Login / Perfil)
CREATE TABLE IF NOT EXISTS cuentas (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 2. TABLA: configuracion_descanso (Horarios y metas configurados en Configurar Descanso)
CREATE TABLE IF NOT EXISTS configuracion_descanso (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 3. TABLA: registros_diarios (Consolidado de sueño para el Dashboard, Historial Mensual y Análisis Semanal)
CREATE TABLE IF NOT EXISTS registros_diarios (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 4. TABLA: monitoreo_sueno (Sesiones nocturnas medidas con el acelerómetro en Monitoreo)
CREATE TABLE IF NOT EXISTS monitoreo_sueno (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 5. TABLA: encuestas_diarias (Respuestas del cuestionario matutino con emojis y factores en Registro Diario)
CREATE TABLE IF NOT EXISTS encuestas_diarias (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 6. TABLA: recomendaciones (Consejos de higiene del sueño por las categorías del app: Horarios, Pantallas, Estrés, Cafeína)
CREATE TABLE IF NOT EXISTS recomendaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    categoria VARCHAR(50) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    resumen VARCHAR(255) NOT NULL,
    descripcion TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- DATOS INICIALES DE RECOMENDACIONES (Las 4 categorías de la app)
INSERT IGNORE INTO recomendaciones (id, categoria, titulo, resumen, descripcion) VALUES
(1, 'Horarios', 'Mantén un horario regular', 'Acuéstate y levántate a la misma hora', 'Mantener una rutina constante ayuda a sincronizar tu reloj biológico y alcanzar fases de sueño profundo con mayor facilidad.'),
(2, 'Pantallas', 'Filtro de luz azul nocturno', 'Reduce pantallas 45 minutos antes de dormir', 'La luz azul emitida por smartphones retrasa la liberación natural de melatonina, dificultando la conciliación rápida del sueño.'),
(3, 'Estrés', 'Respiración diafragmática 4-7-8', 'Inhala 4s, retén 7s y exhala 8s antes de acostarte', 'Ayuda a reducir la frecuencia cardíaca y la tensión muscular acumulada durante la jornada universitaria.'),
(4, 'Cafeína', 'Límite de consumo vespertino', 'Evita bebidas con cafeína 6 horas antes de dormir', 'La cafeína permanece activa en tu organismo varias horas, fragmentando el descanso y aumentando las microinterrupciones nocturnas.');
