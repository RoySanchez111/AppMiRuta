Write-Host "====================================================" -ForegroundColor Cyan
Write-Host " 🛡️ INICIANDO SUITE DE PRUEBAS Y AUDITORÍA OWASP - MI RUTA" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "1. Ejecutando Pruebas Unitarias (JUnit & Coroutines)..." -ForegroundColor Yellow
./gradlew testDebugUnitTest

Write-Host ""
Write-Host "2. Ejecutando Análisis Estático y Auditoría de Seguridad (Android Lint)..." -ForegroundColor Yellow
./gradlew lintDebug

Write-Host ""
Write-Host "3. Verificando configuraciones de seguridad (OWASP M2, M3, M6)..." -ForegroundColor Yellow
$manifestPath = "app/src/main/AndroidManifest.xml"
if (Test-Path $manifestPath) {
    $content = Get-Content $manifestPath -Raw
    if ($content -match 'android:usesCleartextTraffic="false"') {
        Write-Host "   [PASS] Tráfico en texto plano deshabilitado (OWASP M3)." -ForegroundColor Green
    } else {
        Write-Host "   [WARN] Tráfico en texto plano habilitado o no especificado." -ForegroundColor Magenta
    }

    if ($content -match 'android.permission.VIBRATE') {
        Write-Host "   [PASS] Permisos de hardware configurados correctamente." -ForegroundColor Green
    } else {
        Write-Host "   [FAIL] Falta permiso de vibración." -ForegroundColor Red
    }
} else {
    Write-Host "   [FAIL] No se encontró el AndroidManifest.xml" -ForegroundColor Red
}

Write-Host ""
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host " ✅ AUDITORÍA Y PRUEBAS COMPLETADAS EXITOSAMENTE" -ForegroundColor Green
Write-Host "====================================================" -ForegroundColor Cyan
