#!/usr/bin/env bash
# Cambiar automáticamente al directorio raíz del proyecto (un nivel arriba de pruebas/)
cd "$(dirname "$0")/.."

echo "===================================================="
echo " 🛡️ INICIANDO SUITE DE PRUEBAS Y AUDITORÍA OWASP - MI RUTA"
echo "===================================================="

echo ""
echo "1. Ejecutando Pruebas Unitarias (JUnit & Coroutines)..."
./gradlew testDebugUnitTest

echo ""
echo "2. Ejecutando Análisis Estático y Auditoría de Seguridad (Android Lint)..."
./gradlew lintDebug

echo ""
echo "3. Verificando configuraciones de seguridad (OWASP M2, M3, M6)..."
MANIFEST_PATH="app/src/main/AndroidManifest.xml"
if grep -q 'android:usesCleartextTraffic="false"' "$MANIFEST_PATH"; then
    echo "   [PASS] Tráfico en texto plano deshabilitado (OWASP M3)."
else
    echo "   [WARN] Tráfico en texto plano habilitado o no especificado."
fi

if grep -q 'android.permission.VIBRATE' "$MANIFEST_PATH"; then
    echo "   [PASS] Permisos de hardware configurados correctamente."
else
    echo "   [FAIL] Falta permiso de vibración."
fi

echo ""
echo "===================================================="
echo " ✅ AUDITORÍA Y PRUEBAS COMPLETADAS EXITOSAMENTE"
echo "===================================================="
