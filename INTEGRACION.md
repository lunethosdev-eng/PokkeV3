# Pokke — Integración de funciones sociales

## Qué se incluye

1. **Icono** de Pokke (nota musical) en todas las densidades Android
2. **Script de renombrado** `RENAME_TO_POKKE.sh`
3. **Código social** conectado a tu **Kokoro Supabase**:
   - Perfiles (banner, bio, avatar, contadores)
   - Amigos / Seguidores / Siguiendo
   - Chats (conversaciones y mensajes)

## Backend ya listo (Kokoro Supabase)

Tu proyecto ya tiene estas tablas:
- `profiles` (display_name, username, bio, avatar_url, banner_url, followers_count...)
- `profile_follows`
- `conversations` + `conversation_members` + `messages`
- `message_requests`

URL: `https://ajbmpgnzkgtcmulocftd.supabase.co`

## Pasos para integrar

### 1. Extraer / clonar SimpMusic
```bash
# Si tienes el zip:
unzip SimpMusic-dev.zip
cd SimpMusic-dev

# O clonar:
git clone --depth 1 --branch dev https://github.com/maxrave-dev/SimpMusic.git Pokke
cd Pokke
```

### 2. Ejecutar el renombrado
```bash
# Copia el script a la raíz del proyecto
cp /ruta/a/PokkeSocial/RENAME_TO_POKKE.sh .
bash RENAME_TO_POKKE.sh
```

### 3. Copiar el icono
```bash
# Desde la carpeta PokkeSocial
cp -r icon/mipmap-* androidApp/src/main/res/
# (ajusta la ruta si tu módulo android se llama diferente)
```

### 4. Agregar dependencia de Supabase (Kotlin Multiplatform)

En `composeApp/build.gradle.kts` (o el módulo common):

```kotlin
commonMain.dependencies {
    // ... dependencias existentes ...
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.5.0")
    implementation("io.ktor:ktor-client-cio:2.3.12") // o el engine que ya uses
}
```

### 5. Copiar el código social

Copia las carpetas:
```
PokkeSocial/data/          →  composeApp/src/commonMain/kotlin/com/pokke/music/data/
PokkeSocial/viewmodel/     →  composeApp/src/commonMain/kotlin/com/pokke/music/viewmodel/
PokkeSocial/screens/       →  composeApp/src/commonMain/kotlin/com/pokke/music/ui/screen/social/
```

### 6. Registrar las rutas de navegación

En tu grafo de navegación (normalmente `AppNavigationGraph.kt` o similar) agrega:

```kotlin
// Destinos
composable("profile/{userId}") { backStackEntry ->
    val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
    ProfileScreen(
        userId = userId,
        currentUserId = /* tu user id actual */,
        viewModel = profileViewModel,
        onBack = { navController.popBackStack() },
        onOpenFriends = { navController.navigate("friends/$userId") },
        onOpenChat = { username -> /* navegar a chat */ }
    )
}

composable("friends/{userId}") { ... FriendsScreen ... }
composable("chats") { ... ChatListScreen ... }
composable("chat/{conversationId}") { ... ChatConversationScreen ... }
```

### 7. Punto de entrada recomendado

Agrega un ítem en la barra de navegación inferior o en el menú de Library/Settings:

- **Perfil** → navega a `profile/{miUserId}`
- **Amigos** → `friends/{miUserId}`
- **Mensajes** → `chats`

## Notas importantes

- El `userId` de los perfiles es el UUID de `auth.users` de Supabase.
- Para que un usuario tenga perfil, debe existir una fila en `profiles` con el mismo `id`.
- Puedes crear un trigger en Supabase que inserte automáticamente en `profiles` cuando se registra un usuario nuevo.
- Los chats usan `nickname` (username) en las tablas de mensajes. Asegúrate de que el username esté rellenado.

## GPL-3.0

SimpMusic es GPL-3.0. Al modificarlo y redistribuirlo debes:
- Mantener la licencia GPL-3.0
- Indicar que es una versión modificada (Pokke)
- Incluir el código fuente de los cambios

¡Listo! Con esto tienes la base social completa encima de SimpMusic, renombrada a Pokke y conectada a tu Kokoro Supabase.
