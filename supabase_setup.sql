-- ============================================================
-- Pokke — SQL para Kokoro Supabase
-- Ejecutar en el SQL Editor de Supabase
-- ============================================================

-- 1. Trigger: crear perfil automáticamente al registrarse
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER SET search_path = public
AS $$
BEGIN
  INSERT INTO public.profiles (id, display_name, username, bio, created_at, updated_at)
  VALUES (
    NEW.id,
    COALESCE(NEW.raw_user_meta_data->>'display_name', split_part(NEW.email, '@', 1)),
    COALESCE(NEW.raw_user_meta_data->>'username', split_part(NEW.email, '@', 1)),
    '',
    now(),
    now()
  )
  ON CONFLICT (id) DO NOTHING;
  RETURN NEW;
END;
$$;

-- Eliminar trigger anterior si existe
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 2. Actualizar contadores de followers/following (opcional pero recomendado)
CREATE OR REPLACE FUNCTION public.update_follow_counts()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER SET search_path = public
AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    UPDATE profiles SET following_count = following_count + 1 WHERE id = NEW.follower_id;
    UPDATE profiles SET followers_count = followers_count + 1 WHERE id = NEW.following_id;
  ELSIF TG_OP = 'DELETE' THEN
    UPDATE profiles SET following_count = GREATEST(following_count - 1, 0) WHERE id = OLD.follower_id;
    UPDATE profiles SET followers_count = GREATEST(followers_count - 1, 0) WHERE id = OLD.following_id;
  END IF;
  RETURN COALESCE(NEW, OLD);
END;
$$;

DROP TRIGGER IF EXISTS on_follow_change ON public.profile_follows;

CREATE TRIGGER on_follow_change
  AFTER INSERT OR DELETE ON public.profile_follows
  FOR EACH ROW EXECUTE FUNCTION public.update_follow_counts();

-- 3. Políticas RLS básicas (ajusta según necesites)
-- Permitir leer perfiles públicos
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Perfiles públicos visibles" ON public.profiles;
CREATE POLICY "Perfiles públicos visibles" ON public.profiles
  FOR SELECT USING (is_public = true OR auth.uid() = id);

DROP POLICY IF EXISTS "Usuario edita su perfil" ON public.profiles;
CREATE POLICY "Usuario edita su perfil" ON public.profiles
  FOR UPDATE USING (auth.uid() = id);

DROP POLICY IF EXISTS "Usuario inserta su perfil" ON public.profiles;
CREATE POLICY "Usuario inserta su perfil" ON public.profiles
  FOR INSERT WITH CHECK (auth.uid() = id);

-- Follows
ALTER TABLE public.profile_follows ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Ver follows" ON public.profile_follows;
CREATE POLICY "Ver follows" ON public.profile_follows FOR SELECT USING (true);

DROP POLICY IF EXISTS "Seguir" ON public.profile_follows;
CREATE POLICY "Seguir" ON public.profile_follows
  FOR INSERT WITH CHECK (auth.uid() = follower_id);

DROP POLICY IF EXISTS "Dejar de seguir" ON public.profile_follows;
CREATE POLICY "Dejar de seguir" ON public.profile_follows
  FOR DELETE USING (auth.uid() = follower_id);

-- Mensajes (lectura para miembros — simplificado)
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversation_members ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Leer mensajes" ON public.messages;
CREATE POLICY "Leer mensajes" ON public.messages FOR SELECT USING (true);

DROP POLICY IF EXISTS "Enviar mensajes" ON public.messages;
CREATE POLICY "Enviar mensajes" ON public.messages FOR INSERT WITH CHECK (true);

DROP POLICY IF EXISTS "Leer conversaciones" ON public.conversations;
CREATE POLICY "Leer conversaciones" ON public.conversations FOR SELECT USING (true);

DROP POLICY IF EXISTS "Crear conversaciones" ON public.conversations;
CREATE POLICY "Crear conversaciones" ON public.conversations FOR INSERT WITH CHECK (true);

DROP POLICY IF EXISTS "Miembros" ON public.conversation_members;
CREATE POLICY "Miembros" ON public.conversation_members FOR ALL USING (true);

SELECT 'Pokke SQL setup completado ✓' AS status;
