$base_path = "C:\xampp\htdocs\SLEEPTRACKAPP\app\src\main\java\com\example\sleeptrackapp"
$files = Get-ChildItem -Path $base_path -Filter *.java -File

foreach ($f in $files) {
    # Read raw bytes to remove BOM if present
    $bytes = [System.IO.File]::ReadAllBytes($f.FullName)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $newBytes = new-object byte[] ($bytes.Length - 3)
        [System.Array]::Copy($bytes, 3, $newBytes, 0, $newBytes.Length)
        [System.IO.File]::WriteAllBytes($f.FullName, $newBytes)
    }
}
Write-Host "BOM removed"
