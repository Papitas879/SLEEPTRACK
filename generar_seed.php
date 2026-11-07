<?php

$hash_password = password_hash('Sleep2026!', PASSWORD_BCRYPT);

$cuentas = [
    [
        'nombres' => 'Jose Pablo Lopez Arellano',
        'correo' => 'jose.lopez@upn.pe',
        'password' => $hash_password,
        'fecha_nacimiento' => '14/03/2005',
        'universidad' => 'Universidad Privada del Norte (UPN)',
        'carrera' => 'Ingeniería de Sistemas',
        'hora_dormir' => '23:00',
        'hora_despertar' => '07:00',
        'meta_horas' => '8 horas',
        'profile' => 'sistemas'
    ],
    [
        'nombres' => 'Kevin Anderson Ortiz More',
        'correo' => 'kevin.ortiz@unmsm.edu.pe',
        'password' => $hash_password,
        'fecha_nacimiento' => '28/07/2005',
        'universidad' => 'Universidad Nacional Mayor de San Marcos (UNMSM)',
        'carrera' => 'Medicina Humana',
        'hora_dormir' => '00:00',
        'hora_despertar' => '07:00',
        'meta_horas' => '7 horas',
        'profile' => 'medicina'
    ],
    [
        'nombres' => 'Gerson Manuel Soto Principe',
        'correo' => 'gerson.soto@uni.edu.pe',
        'password' => $hash_password,
        'fecha_nacimiento' => '19/11/2005',
        'universidad' => 'Universidad Nacional de Ingeniería (UNI)',
        'carrera' => 'Ingeniería Industrial',
        'hora_dormir' => '23:30',
        'hora_despertar' => '06:30',
        'meta_horas' => '7 horas',
        'profile' => 'industrial'
    ],
    [
        'nombres' => 'Anderson Socimo Tarazona Saenz',
        'correo' => 'anderson.tarazona@upn.pe',
        'password' => $hash_password,
        'fecha_nacimiento' => '05/02/2006',
        'universidad' => 'Universidad Privada del Norte (UPN)',
        'carrera' => 'Ingeniería de Software',
        'hora_dormir' => '23:00',
        'hora_despertar' => '07:00',
        'meta_horas' => '8 horas',
        'profile' => 'software'
    ],
    [
        'nombres' => 'Valeria Nicole Ramos Castro',
        'correo' => 'valeria.ramos@pucp.edu.pe',
        'password' => $hash_password,
        'fecha_nacimiento' => '22/09/2005',
        'universidad' => 'Pontificia Universidad Católica del Perú (PUCP)',
        'carrera' => 'Psicología',
        'hora_dormir' => '22:30',
        'hora_despertar' => '06:30',
        'meta_horas' => '8 horas',
        'profile' => 'psicologia'
    ]
];

$calidades = ['Muy mala', 'Mala', 'Regular', 'Buena', 'Muy buena'];
$energias = ['Muy baja', 'Baja', 'Media', 'Alta', 'Muy alta'];
$estres_opts = ['Bajo', 'Medio', 'Alto'];
$cafeina_opts = ['No consumí', 'Sí, una vez', 'Sí, más de una vez'];
$pantallas_opts = ['Menos de 30 minutos', '30 – 60 minutos', 'Más de 60 minutos'];

$sql_lines = [];
$sql_lines[] = "USE `sleeptrack`;";
$sql_lines[] = "SET FOREIGN_KEY_CHECKS = 0;";

foreach ($cuentas as $c) {
    $nombres = addslashes($c['nombres']);
    $correo = addslashes($c['correo']);
    $pass = $c['password'];
    $fnac = $c['fecha_nacimiento'];
    $univ = addslashes($c['universidad']);
    $carr = addslashes($c['carrera']);
    $hdorm = $c['hora_dormir'];
    $hdesp = $c['hora_despertar'];
    $meta = $c['meta_horas'];

    $sql_lines[] = "INSERT INTO `cuentas` (`nombres`, `correo`, `password`, `fecha_nacimiento`, `universidad`, `carrera`, `hora_dormir`, `hora_despertar`, `meta_horas`, `sensores_aceptados`) " .
                   "VALUES ('$nombres', '$correo', '$pass', '$fnac', '$univ', '$carr', '$hdorm', '$hdesp', '$meta', 1) " .
                   "ON DUPLICATE KEY UPDATE `password` = VALUES(`password`), `fecha_nacimiento` = VALUES(`fecha_nacimiento`), `universidad` = VALUES(`universidad`), `carrera` = VALUES(`carrera`);";

    $sql_lines[] = "INSERT INTO `configuracion_descanso` (`correo`, `hora_dormir`, `hora_despertar`, `meta_horas`, `recordatorios_activos`, `sensores_aceptados`) " .
                   "VALUES ('$correo', '$hdorm', '$hdesp', '$meta', 1, 1) " .
                   "ON DUPLICATE KEY UPDATE `hora_dormir` = VALUES(`hora_dormir`), `hora_despertar` = VALUES(`hora_despertar`), `meta_horas` = VALUES(`meta_horas`);";
}

$fecha_inicio = new DateTime('2026-09-01');
$fecha_fin = new DateTime('2026-10-30');

foreach ($cuentas as $idx => $c) {
    $correo = addslashes($c['correo']);
    $profile = $c['profile'];
    $meta_min = ($c['meta_horas'] === '7 horas') ? 420 : 480;

    $current = clone $fecha_inicio;
    $seed_salt = $idx * 17;

    while ($current <= $fecha_fin) {
        $fecha_str = $current->format('Y-m-d');
        $dia_semana = (int)$current->format('N');
        $es_fin_semana = ($dia_semana >= 6);
        $dia_mes = (int)$current->format('d');
        $mes = (int)$current->format('m');

        $skip_prob = (($dia_mes + $seed_salt) % 9 === 0);
        if ($skip_prob) {
            $current->modify('+1 day');
            continue;
        }

        $en_parciales = ($mes == 10 && $dia_mes >= 5 && $dia_mes <= 16);

        if ($en_parciales) {

            $duracion = rand(330, 420);
            $calidad_pct = rand(45, 75);
            $eficiencia = rand(70, 85);
            $movimientos = rand(15, 30);
            $estres = (rand(0, 10) > 2) ? 'Alto' : 'Medio';
            $cafeina = (rand(0, 10) > 3) ? 'Sí, más de una vez' : 'Sí, una vez';
            $pantallas = (rand(0, 10) > 4) ? 'Más de 60 minutos' : '30 – 60 minutos';
            $interrupciones = (string)rand(2, 4);
            $energia = (rand(0, 10) > 3) ? 'Baja' : 'Media';
            $h_inicio = sprintf("%02d:%02d", rand(0, 1), rand(10, 50));
            $h_fin = sprintf("%02d:%02d", rand(6, 7), rand(0, 30));
        } elseif ($es_fin_semana) {

            $duracion = rand(450, 540);
            $calidad_pct = rand(78, 96);
            $eficiencia = rand(85, 96);
            $movimientos = rand(5, 16);
            $estres = 'Bajo';
            $cafeina = (rand(0, 10) > 6) ? 'Sí, una vez' : 'No consumí';
            $pantallas = (rand(0, 10) > 5) ? '30 – 60 minutos' : 'Menos de 30 minutos';
            $interrupciones = (string)rand(0, 1);
            $energia = (rand(0, 10) > 3) ? 'Alta' : 'Muy alta';
            $h_inicio = sprintf("%02d:%02d", 0, rand(15, 55));
            $h_fin = sprintf("%02d:%02d", rand(8, 9), rand(0, 45));
        } else {

            $duracion = rand(390, 490);
            $calidad_pct = rand(65, 88);
            $eficiencia = rand(78, 92);
            $movimientos = rand(8, 22);
            $estres = (rand(0, 10) > 5) ? 'Medio' : 'Bajo';
            $cafeina = (rand(0, 10) > 4) ? 'Sí, una vez' : 'No consumí';
            $pantallas = (rand(0, 10) > 4) ? '30 – 60 minutos' : 'Menos de 30 minutos';
            $interrupciones = (string)rand(0, 2);
            $energia = (rand(0, 10) > 4) ? 'Media' : 'Alta';
            $h_inicio = sprintf("%02d:%02d", 23, rand(0, 45));
            $h_fin = sprintf("%02d:%02d", 6, rand(45, 59));
        }

        if ($calidad_pct >= 85) {
            $calidad_texto = 'Muy buena';
        } elseif ($calidad_pct >= 70) {
            $calidad_texto = 'Buena';
        } elseif ($calidad_pct >= 50) {
            $calidad_texto = 'Regular';
        } elseif ($calidad_pct >= 35) {
            $calidad_texto = 'Mala';
        } else {
            $calidad_texto = 'Muy mala';
        }

        $eventos_pantalla = ($pantallas === 'Más de 60 minutos') ? '3 eventos' : (($pantallas === '30 – 60 minutos') ? '1 evento' : '0 eventos');

        $sql_lines[] = "INSERT INTO `registros_diarios` (`correo`, `calidad`, `energia`, `interrupciones`, `estres`, `cafeina`, `pantallas`, `duracion_minutos`, `hora_inicio`, `hora_fin`, `eficiencia`, `calidad_porcentaje`, `movimientos`, `fecha`) " .
                       "VALUES ('$correo', '$calidad_texto', '$energia', '$interrupciones', '$estres', '$cafeina', '$pantallas', $duracion, '$h_inicio', '$h_fin', $eficiencia, $calidad_pct, $movimientos, '$fecha_str') " .
                       "ON DUPLICATE KEY UPDATE `duracion_minutos` = VALUES(`duracion_minutos`), `eficiencia` = VALUES(`eficiencia`), `calidad_porcentaje` = VALUES(`calidad_porcentaje`), `calidad` = VALUES(`calidad`), `energia` = VALUES(`energia`), `estres` = VALUES(`estres`), `cafeina` = VALUES(`cafeina`), `pantallas` = VALUES(`pantallas`);";

        $sql_lines[] = "INSERT INTO `monitoreo_sueno` (`correo`, `fecha`, `hora_inicio`, `hora_fin`, `duracion_minutos`, `movimientos`, `eventos_pantalla`, `eficiencia`, `calidad_porcentaje`) " .
                       "VALUES ('$correo', '$fecha_str', '$h_inicio', '$h_fin', $duracion, $movimientos, '$eventos_pantalla', $eficiencia, $calidad_pct);";

        $sql_lines[] = "INSERT INTO `encuestas_diarias` (`correo`, `fecha`, `calidad_percibida`, `nivel_energia`, `interrupciones`, `nivel_estres`, `consumo_cafeina`, `exposicion_pantallas`) " .
                       "VALUES ('$correo', '$fecha_str', '$calidad_texto', '$energia', '$interrupciones', '$estres', '$cafeina', '$pantallas');";

        $current->modify('+1 day');
    }
}

$sql_lines[] = "SET FOREIGN_KEY_CHECKS = 1;";

$sql_content = "\xEF\xBB\xBF" . implode("\n", $sql_lines);
file_put_contents('c:/xampp/htdocs/SLEEPTRACKAPP/seed_data.sql', $sql_content);

echo "Archivo SQL generado exitosamente con " . count($sql_lines) . " sentencias.\n";
