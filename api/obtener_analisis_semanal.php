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
    $offset_semanas = isset($data['offset_semanas']) ? (int)$data['offset_semanas'] : 0;

    $meta_minutos = 480;
    $meta_texto = "8h 00m";
    $h_dormir_obj = "23:00";
    $h_despertar_obj = "07:00";

    $stmtUser = $conn->prepare("SELECT hora_dormir, hora_despertar, meta_horas FROM cuentas WHERE correo = ?");
    if ($stmtUser) {
        $stmtUser->bind_param("s", $correo);
        $stmtUser->execute();
        $userRes = $stmtUser->get_result();
        if ($userRow = $userRes->fetch_assoc()) {
            if (!empty($userRow['hora_dormir'])) $h_dormir_obj = $userRow['hora_dormir'];
            if (!empty($userRow['hora_despertar'])) $h_despertar_obj = $userRow['hora_despertar'];
            if (!empty($userRow['meta_horas'])) {
                $meta_texto = $userRow['meta_horas'];
                if (preg_match('/(\d+)\s*horas?/i', $meta_texto, $m)) {
                    $meta_minutos = ((int)$m[1]) * 60;
                }
            }
        }
        $stmtUser->close();
    }

    $ts_referencia = strtotime("$offset_semanas week");

    $dia_semana = (int)date('N', $ts_referencia);
    $lunes_ts = strtotime("-" . ($dia_semana - 1) . " days", $ts_referencia);
    $domingo_ts = strtotime("+6 days", $lunes_ts);

    $lunes_fecha = date('Y-m-d', $lunes_ts);
    $domingo_fecha = date('Y-m-d', $domingo_ts);

    $etiqueta_semana = "Semana del " . date('d', $lunes_ts) . " al " . date('d', $domingo_ts) . " de " . obtenerNombreMes(date('n', $domingo_ts));

    $stmt = $conn->prepare("SELECT * FROM registros_diarios WHERE correo = ? AND fecha BETWEEN ? AND ? ORDER BY fecha ASC");
    $registros_map = [];
    if ($stmt) {
        $stmt->bind_param("sss", $correo, $lunes_fecha, $domingo_fecha);
        $stmt->execute();
        $res = $stmt->get_result();
        while ($row = $res->fetch_assoc()) {
            $registros_map[$row['fecha']] = $row;
        }
        $stmt->close();
    }

    $nombres_dias = ["Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"];
    $dias = [];
    $total_minutos = 0;
    $dias_con_registro = 0;
    $noches_cumplidas = 0;

    $minutos_semana = [];
    $minutos_finde = [];

    for ($i = 0; $i < 7; $i++) {
        $cur_ts = strtotime("+$i days", $lunes_ts);
        $cur_fecha = date('Y-m-d', $cur_ts);
        $es_finde = ($i >= 5);

        $dur_min = 0;
        $cal_pct = 0;
        $cal_txt = "-";
        $h_ini = "";
        $h_fin = "";
        $cumplio = false;
        $tiene_registro = false;

        if (isset($registros_map[$cur_fecha])) {
            $r = $registros_map[$cur_fecha];
            $tiene_registro = true;
            $dur_min = (int)$r['duracion_minutos'];
            $cal_pct = (int)$r['calidad_porcentaje'];
            $cal_txt = !empty($r['calidad']) ? $r['calidad'] : "Regular";
            $h_ini = !empty($r['hora_inicio']) ? $r['hora_inicio'] : $h_dormir_obj;
            $h_fin = !empty($r['hora_fin']) ? $r['hora_fin'] : $h_despertar_obj;

            if ($dur_min >= $meta_minutos * 0.9 && $dur_min > 0) {
                $cumplio = true;
                $noches_cumplidas++;
            }

            $total_minutos += $dur_min;
            $dias_con_registro++;

            if ($es_finde) {
                $minutos_finde[] = $dur_min;
            } else {
                $minutos_semana[] = $dur_min;
            }
        } else {

            $dur_min = 0;
        }

        $horas_num = round($dur_min / 60.0, 1);
        $h_int = floor($dur_min / 60);
        $m_int = $dur_min % 60;
        $dur_formateada = $tiene_registro ? "{$h_int}h " . sprintf("%02dm", $m_int) : "-";

        $dias[] = [
            "dia_nombre" => $nombres_dias[$i],
            "fecha" => $cur_fecha,
            "dia_mes" => (int)date('d', $cur_ts),
            "duracion_minutos" => $dur_min,
            "duracion_horas" => $horas_num,
            "duracion_formateada" => $dur_formateada,
            "cumplio" => $cumplio,
            "tiene_registro" => $tiene_registro,
            "hora_inicio" => $h_ini,
            "hora_fin" => $h_fin,
            "calidad" => $cal_txt,
            "es_fin_de_semana" => $es_finde
        ];
    }

    $promedio_min = $dias_con_registro > 0 ? (int)round($total_minutos / $dias_con_registro) : 0;
    $prom_h = floor($promedio_min / 60);
    $prom_m = $promedio_min % 60;
    $promedio_texto = $dias_con_registro > 0 ? "{$prom_h}h " . sprintf("%02dm", $prom_m) : "0h 00m";

    $habito_sem_texto = "Sin registros";
    $habito_finde_texto = "Sin registros";
    $irregularidad_alta = false;

    if (count($minutos_semana) > 0) {
        $prom_sem_min = round(array_sum($minutos_semana) / count($minutos_semana));
        $habito_sem_texto = "~" . floor($prom_sem_min / 60) . "h " . sprintf("%02dm", $prom_sem_min % 60) . " promedio";
    }

    if (count($minutos_finde) > 0) {
        $prom_finde_min = round(array_sum($minutos_finde) / count($minutos_finde));
        $habito_finde_texto = "~" . floor($prom_finde_min / 60) . "h " . sprintf("%02dm", $prom_finde_min % 60) . " promedio";
    }

    if (count($minutos_semana) > 0 && count($minutos_finde) > 0) {
        $diferencia_min = abs($prom_finde_min - $prom_sem_min);
        $irregularidad_alta = ($diferencia_min >= 60);
    }

    echo json_encode([
        "status" => "success",
        "etiqueta_semana" => $etiqueta_semana,
        "offset_semanas" => $offset_semanas,
        "promedio_texto" => $promedio_texto,
        "promedio_minutos" => $promedio_min,
        "dias_medidos" => $dias_con_registro,
        "noches_cumplidas" => $noches_cumplidas,
        "total_dias" => 7,
        "cumplimiento_ratio" => "$noches_cumplidas de 7",
        "habito_semana" => $habito_sem_texto,
        "habito_finde" => $habito_finde_texto,
        "irregularidad_detectada" => $irregularidad_alta,
        "meta_texto" => $meta_texto,
        "dias" => $dias
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Correo no proporcionado."]);
}

function obtenerNombreMes($mesNum) {
    $meses = [1=>"Ene", 2=>"Feb", 3=>"Mar", 4=>"Abr", 5=>"May", 6=>"Jun", 7=>"Jul", 8=>"Ago", 9=>"Sep", 10=>"Oct", 11=>"Nov", 12=>"Dic"];
    return isset($meses[$mesNum]) ? $meses[$mesNum] : "Mes";
}

$conn->close();
?>
