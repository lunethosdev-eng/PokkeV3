#!/bin/bash
# ============================================================
#  POKKE SETUP
#  Usa el código de SimpMusic + funciones sociales + icono
#  Solo incluye lo necesario (sin .claude, fastlane, assets extra, etc.)
# ============================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WORK_DIR="$(pwd)"

echo ""
echo "╔══════════════════════════════════════════╗"
echo "║         POKKE — Setup automático         ║"
echo "╚══════════════════════════════════════════╝"
echo ""

# --- 1. Localizar el zip de SimpMusic ---
ZIP=""
if [ -f "$WORK_DIR/SimpMusic-dev.zip" ]; then
  ZIP="$WORK_DIR/SimpMusic-dev.zip"
elif [ -f "$SCRIPT_DIR/../SimpMusic-dev.zip" ]; then
  ZIP="$SCRIPT_DIR/../SimpMusic-dev.zip"
elif [ -f "$HOME/Downloads/SimpMusic-dev.zip" ]; then
  ZIP="$HOME/Downloads/SimpMusic-dev.zip"
else
  echo "❌ No encontré SimpMusic-dev.zip"
  echo "   Colócalo en la misma carpeta donde ejecutas este script."
  echo "   O pásalo como argumento:  bash setup_pokke.sh /ruta/a/SimpMusic-dev.zip"
  if [ -n "$1" ] && [ -f "$1" ]; then
    ZIP="$1"
  else
    exit 1
  fi
fi

echo "✓ Zip encontrado: $ZIP"
echo ""

# --- 2. Extraer solo lo necesario ---
OUT="$WORK_DIR/Pokke"
rm -rf "$OUT"
mkdir -p "$OUT"

echo "→ Extrayendo módulos esenciales (sin basura)..."

# Lista de lo que SÍ queremos
ESSENTIAL=(
  "SimpMusic-dev/androidApp"
  "SimpMusic-dev/composeApp"
  "SimpMusic-dev/desktopApp"
  "SimpMusic-dev/vivoAndroidApp"
  "SimpMusic-dev/cast"
  "SimpMusic-dev/cast-empty"
  "SimpMusic-dev/crashlytics"
  "SimpMusic-dev/crashlytics-empty"
  "SimpMusic-dev/lastfm"
  "SimpMusic-dev/lastfm-empty"
  "SimpMusic-dev/gradle"
  "SimpMusic-dev/config"
  "SimpMusic-dev/build.gradle.kts"
  "SimpMusic-dev/settings.gradle.kts"
  "SimpMusic-dev/gradle.properties"
  "SimpMusic-dev/gradlew"
  "SimpMusic-dev/gradlew.bat"
  "SimpMusic-dev/LICENSE"
  "SimpMusic-dev/README.md"
)

for item in "${ESSENTIAL[@]}"; do
  unzip -qo "$ZIP" "$item/*" -d "$OUT/tmp" 2>/dev/null || \
  unzip -qo "$ZIP" "$item" -d "$OUT/tmp" 2>/dev/null || true
done

# Mover al nivel correcto
if [ -d "$OUT/tmp/SimpMusic-dev" ]; then
  mv "$OUT/tmp/SimpMusic-dev"/* "$OUT/"
  rm -rf "$OUT/tmp"
fi

# Crear carpeta core vacía (submódulo)
mkdir -p "$OUT/core"
echo "✓ Extracción lista"

# --- 3. Clonar el submódulo core (necesario para compilar) ---
echo "→ Clonando core (submódulo necesario)..."
if command -v git >/dev/null 2>&1; then
  git clone --depth 1 https://github.com/maxrave-dev/core.git "$OUT/core" 2>/dev/null || \
  echo "  ⚠ No se pudo clonar core automáticamente. Ejecuta después:"
  echo "     cd Pokke && git clone --depth 1 https://github.com/maxrave-dev/core.git core"
else
  echo "  ⚠ git no está instalado. Clona core manualmente:"
  echo "     cd Pokke && git clone --depth 1 https://github.com/maxrave-dev/core.git core"
fi

# --- 4. Renombrar SimpMusic → Pokke ---
echo "→ Renombrando a Pokke..."

# Textos
find "$OUT" -type f \( -name "*.kt" -o -name "*.kts" -o -name "*.xml" -o -name "*.gradle" -o -name "*.properties" -o -name "*.md" -o -name "*.toml" \) \
  -not -path "*/.git/*" -not -path "*/build/*" \
  -exec sed -i \
    -e 's/com\.maxrave\.simpmusic/com.pokke.music/g' \
    -e 's|com/maxrave/simpmusic|com/pokke/music|g' \
    -e 's/SimpMusic/Pokke/g' \
    -e 's/simpmusic/pokke/g' \
    -e 's/Simp Music/Pokke/g' \
    -e 's/maxrave-dev/pokke/g' \
    {} + 2>/dev/null || true

# Carpetas de package
find "$OUT" -type d -path "*/com/maxrave/simpmusic" 2>/dev/null | while read -r dir; do
  parent=$(dirname "$dir")
  mkdir -p "$parent/pokke/music"
  if [ -d "$dir" ]; then
    cp -a "$dir"/. "$parent/pokke/music/" 2>/dev/null || true
    rm -rf "$(dirname "$parent")/maxrave" 2>/dev/null || true
  fi
done

# app_name
find "$OUT" -name "app_name.xml" -exec sed -i 's/>[^<]*</>Pokke</g' {} + 2>/dev/null || true

# rootProject.name
if [ -f "$OUT/settings.gradle.kts" ]; then
  sed -i 's/rootProject\.name.*/rootProject.name = "Pokke"/' "$OUT/settings.gradle.kts"
fi

echo "✓ Renombrado completado"

# --- 5. Icono ---
echo "→ Instalando icono de Pokke..."
RES_DIR="$OUT/androidApp/src/main/res"
if [ -d "$RES_DIR" ]; then
  for density in mdpi hdpi xhdpi xxhdpi xxxhdpi; do
    mkdir -p "$RES_DIR/mipmap-$density"
    if [ -f "$SCRIPT_DIR/icon/mipmap-$density/ic_launcher.png" ]; then
      cp "$SCRIPT_DIR/icon/mipmap-$density/ic_launcher.png" "$RES_DIR/mipmap-$density/"
      cp "$SCRIPT_DIR/icon/mipmap-$density/ic_launcher_round.png" "$RES_DIR/mipmap-$density/" 2>/dev/null || true
    fi
  done
  # Foreground para adaptive icon
  if [ -f "$SCRIPT_DIR/icon/ic_launcher_foreground.png" ]; then
    mkdir -p "$RES_DIR/mipmap-xxxhdpi"
    cp "$SCRIPT_DIR/icon/ic_launcher_foreground.png" "$RES_DIR/mipmap-xxxhdpi/" 2>/dev/null || true
    mkdir -p "$RES_DIR/drawable"
    cp "$SCRIPT_DIR/icon/ic_launcher_foreground.png" "$RES_DIR/drawable/ic_launcher_foreground.png" 2>/dev/null || true
  fi
  echo "✓ Icono instalado"
else
  echo "  ⚠ No se encontró androidApp/src/main/res (icono se copiará a /icon para que lo pongas manualmente)"
  cp -r "$SCRIPT_DIR/icon" "$OUT/icon_pokke"
fi

# --- 6. Código social ---
echo "→ Agregando pantallas sociales (Perfil, Amigos, Chats)..."

SOCIAL_BASE="$OUT/composeApp/src/commonMain/kotlin/com/pokke/music"
# Por si el rename de carpetas no movió todo
if [ ! -d "$SOCIAL_BASE" ]; then
  # Buscar la ruta real
  SOCIAL_BASE=$(find "$OUT/composeApp" -type d -name "music" 2>/dev/null | head -1)
  if [ -z "$SOCIAL_BASE" ]; then
    SOCIAL_BASE="$OUT/composeApp/src/commonMain/kotlin/com/pokke/music"
    mkdir -p "$SOCIAL_BASE"
  fi
fi

mkdir -p "$SOCIAL_BASE/data"
mkdir -p "$SOCIAL_BASE/viewmodel"
mkdir -p "$SOCIAL_BASE/ui/screen/social"

cp "$SCRIPT_DIR/data/"*.kt "$SOCIAL_BASE/data/" 2>/dev/null || true
cp "$SCRIPT_DIR/viewmodel/"*.kt "$SOCIAL_BASE/viewmodel/" 2>/dev/null || true
cp "$SCRIPT_DIR/screens/"*.kt "$SOCIAL_BASE/ui/screen/social/" 2>/dev/null || true

echo "✓ Código social agregado en: $SOCIAL_BASE"

# --- 7. Dependencia Supabase en build.gradle.kts ---
COMPOSE_BUILD="$OUT/composeApp/build.gradle.kts"
if [ -f "$COMPOSE_BUILD" ]; then
  if ! grep -q "supabase" "$COMPOSE_BUILD"; then
    # Intentamos agregar la dependencia de forma segura
    echo "" >> "$COMPOSE_BUILD"
    echo "// --- Pokke Social (Supabase) ---" >> "$COMPOSE_BUILD"
    echo "// Agrega en commonMain.dependencies:" >> "$COMPOSE_BUILD"
    echo "//   implementation(\"io.github.jan-tennert.supabase:postgrest-kt:2.5.0\")" >> "$COMPOSE_BUILD"
    echo "//   implementation(\"io.github.jan-tennert.supabase:gotrue-kt:2.5.0\")" >> "$COMPOSE_BUILD"
    echo "//   implementation(\"io.github.jan-tennert.supabase:realtime-kt:2.5.0\")" >> "$COMPOSE_BUILD"
    echo "//   implementation(\"io.github.jan-tennert.supabase:storage-kt:2.5.0\")" >> "$COMPOSE_BUILD"
  fi
fi

# --- 8. README de Pokke ---
cat > "$OUT/README_POKKE.md" << 'README'
# Pokke

Cliente de música basado en SimpMusic + funciones sociales (perfiles, amigos, chats).

## Backend
- Supabase (Kokoro): `https://ajbmpgnzkgtcmulocftd.supabase.co`
- Tablas: `profiles`, `profile_follows`, `conversations`, `messages`

## Qué se agregó
- Pantallas: Perfil (banner + bio), Amigos, Chats
- ViewModels y repositorio conectados a Supabase
- Icono personalizado
- Package renombrado a `com.pokke.music`

## Cómo compilar

1. Asegúrate de tener el submódulo **core**:
   ```bash
   cd Pokke
   git clone --depth 1 https://github.com/maxrave-dev/core.git core
   ```

2. Agrega las dependencias de Supabase en `composeApp/build.gradle.kts` (commonMain):
   ```kotlin
   implementation("io.github.jan-tennert.supabase:postgrest-kt:2.5.0")
   implementation("io.github.jan-tennert.supabase:gotrue-kt:2.5.0")
   implementation("io.github.jan-tennert.supabase:realtime-kt:2.5.0")
   implementation("io.github.jan-tennert.supabase:storage-kt:2.5.0")
   ```

3. Registra las rutas de navegación (ver INTEGRACION.md original).

4. Sincroniza Gradle y compila:
   ```bash
   ./gradlew :androidApp:assembleDebug
   ```

## Licencia
GPL-3.0 (igual que SimpMusic). Debes mantener la licencia y indicar que es una versión modificada.
README

echo ""
echo "╔══════════════════════════════════════════╗"
echo "║              ¡LISTO!                     ║"
echo "╠══════════════════════════════════════════╣"
echo "║  Carpeta creada:  ./Pokke                ║"
echo "║                                          ║"
echo "║  Siguiente paso:                         ║"
echo "║  1. cd Pokke                             ║"
echo "║  2. git clone --depth 1 \\                ║"
echo "║       https://github.com/maxrave-dev/core.git core"
echo "║  3. Abre en Android Studio               ║"
echo "║  4. Agrega deps de Supabase (ver README) ║"
echo "║  5. Conecta las pantallas en navigation  ║"
echo "╚══════════════════════════════════════════╝"
echo ""
