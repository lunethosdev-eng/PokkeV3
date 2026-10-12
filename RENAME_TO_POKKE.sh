#!/bin/bash
# ============================================================
# Script para renombrar SimpMusic → Pokke
# Ejecutar desde la raíz del proyecto clonado/extraído
# ============================================================

set -e

echo "=== Renombrando SimpMusic a Pokke ==="

# 1. Cambiar package name en todos los archivos Kotlin/Gradle
find . -type f \( -name "*.kt" -o -name "*.kts" -o -name "*.xml" -o -name "*.gradle" -o -name "*.properties" -o -name "*.md" \) \
  -not -path "./.git/*" -not -path "./build/*" -not -path "./.gradle/*" \
  -exec sed -i \
    -e 's/com\.maxrave\.simpmusic/com.pokke.music/g' \
    -e 's/com\/maxrave\/simpmusic/com\/pokke\/music/g' \
    -e 's/SimpMusic/Pokke/g' \
    -e 's/simpmusic/pokke/g' \
    -e 's/Simp Music/Pokke/g' \
    {} +

# 2. Renombrar carpetas de package
echo "Renombrando carpetas de package..."
# Android / common
for dir in $(find . -type d -path "*/com/maxrave/simpmusic" 2>/dev/null); do
  parent=$(dirname "$dir")
  mkdir -p "$parent/pokke/music"
  # Mover contenido
  if [ -d "$dir" ]; then
    mv "$dir"/* "$parent/pokke/music/" 2>/dev/null || true
    # Limpiar
    rm -rf "$parent/maxrave" 2>/dev/null || true
  fi
done

# 3. app_name.xml
find . -name "app_name.xml" -exec sed -i 's/>.*</>Pokke</g' {} +

# 4. settings.gradle.kts rootProject.name
if [ -f settings.gradle.kts ]; then
  sed -i 's/rootProject\.name.*/rootProject.name = "Pokke"/' settings.gradle.kts
fi

echo ""
echo "=== Renombrado completado ==="
echo "Recuerda:"
echo "  1. Copiar los iconos de PokkeSocial/icon/ a androidApp/src/main/res/"
echo "  2. Agregar las pantallas sociales (ver INTEGRACION.md)"
echo "  3. Sincronizar Gradle y recompilar"
