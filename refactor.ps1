$base_path = "C:\xampp\htdocs\SLEEPTRACKAPP\app\src\main"
$java_dir = "$base_path\java\com\example\sleeptrackapp"
$res_dir = "$base_path\res\layout"

$renames = @(
    @("LoginActivity", "Hu03IniciarSesionActivity", "activity_login", "activity_hu03_iniciar_sesion"),
    @("Hu03ConsentimientoActivity", "Hu05ConsentimientoActivity", "activity_hu03_consentimiento", "activity_hu05_consentimiento"),
    @("Hu04ConfigurarDescansoActivity", "Hu06ConfigurarDescansoActivity", "activity_hu04_configurar_descanso", "activity_hu06_configurar_descanso"),
    @("Hu05MonitoreoActivity", "Hu07MonitoreoActivity", "activity_hu05_monitoreo", "activity_hu07_monitoreo"),
    @("Hu10ResumenDescansoActivity", "Hu12ResumenDescansoActivity", "activity_hu10_resumen_descanso", "activity_hu12_resumen_descanso"),
    @("Hu18AlertaLogroActivity", "Hu20AlertaLogroActivity", "activity_hu18_alerta_logro", "activity_hu20_alerta_logro"),
    @("Hu20PrivacidadDatosActivity", "Hu23PrivacidadDatosActivity", "activity_hu20_privacidad_datos", "activity_hu23_privacidad_datos")
)

foreach ($r in $renames) {
    $old_j_path = "$java_dir\$($r[0]).java"
    $new_j_path = "$java_dir\$($r[1]).java"
    $old_x_path = "$res_dir\$($r[2]).xml"
    $new_x_path = "$res_dir\$($r[3]).xml"

    if (Test-Path $old_j_path) { Rename-Item $old_j_path "$($r[1]).java" }
    if (Test-Path $old_x_path) { Rename-Item $old_x_path "$($r[3]).xml" }
}

$files = Get-ChildItem -Path $base_path -Include *.java, *.xml -Recurse
foreach ($f in $files) {
    $content = Get-Content $f.FullName -Raw
    $original = $content
    foreach ($r in $renames) {
        $content = $content -replace $r[0], $r[1]
        $content = $content -replace $r[2], $r[3]
    }
    if ($content -cne $original) {
        Set-Content -Path $f.FullName -Value $content -Encoding UTF8
    }
}
Write-Host "Refactor complete"
