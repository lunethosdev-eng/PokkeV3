# Pokke — Subir a GitHub + Actions desde Termux

## 1. Crear el repo en GitHub (una sola vez)

En el navegador del teléfono entra a:
https://github.com/new

- Nombre: **Pokke**
- Público
- NO marques "Add a README" (lo creamos nosotros)
- Create repository

## 2. En Termux — preparar el proyecto

```bash
pkg update -y
pkg install git unzip openjdk-17 gh -y

# Dar acceso a almacenamiento
termux-setup-storage

cd /sdcard/Download

# Descomprimir el pack (si aún no lo hiciste)
unzip -o Pokke_Completo.zip
cd PokkeSetup

# Generar el proyecto Pokke (necesitas SimpMusic-dev.zip en Download)
bash setup_pokke.sh /sdcard/Download/SimpMusic-dev.zip

cd ../Pokke

# Clonar el core (obligatorio)
git clone --depth 1 https://github.com/maxrave-dev/core.git core

# Copiar el workflow de GitHub Actions
mkdir -p .github/workflows
cp ../PokkeSetup/.github/workflows/build-apk.yml .github/workflows/
```

## 3. Subir a GitHub

```bash
cd /sdcard/Download/Pokke

git init
git branch -M main
git add .
git config user.email "tu@email.com"
git config user.name "lunethosdev-eng"
git commit -m "Pokke: música + perfiles + amigos + chats"

# Login en GitHub (elige HTTPS o el token)
gh auth login
# Sigue las instrucciones (elige GitHub.com → HTTPS → Login with browser o token)

# Crear el repo y subir (si no lo creaste en el paso 1)
gh repo create Pokke --public --source=. --remote=origin --push

# O si ya creaste el repo vacío:
git remote add origin https://github.com/lunethosdev-eng/Pokke.git
git push -u origin main
```

## 4. Compilar el APK con GitHub Actions

1. Entra a: https://github.com/lunethosdev-eng/Pokke/actions
2. Verás el workflow **Build Pokke APK**
3. Cuando termine (10-20 min) baja el APK en:
   - **Artifacts** → Pokke-debug
   - O en **Releases** (tag build-XXX)

También puedes lanzarlo manualmente:
Actions → Build Pokke APK → Run workflow

## Notas

- La primera build puede fallar si falta alguna dependencia o el core no se clonó bien. Revisa los logs en Actions.
- El APK será **debug** (para instalar en tu teléfono). Para release firmado hace falta un keystore.
- Tu Supabase ya está configurado en el código (`SupabaseConfig.kt`).

