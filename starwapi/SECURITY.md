# Autenticación local

Se requiere Java 21 o superior. Antes de arrancar, configura una clave aleatoria de al menos 32 bytes en Base64:

```powershell
$env:JWT_SECRET = [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
./mvnw.cmd spring-boot:run
```

Conserva la misma clave entre reinicios si quieres que sigan siendo válidos los tokens emitidos. En despliegues, usa el gestor de secretos del entorno. No guardes la clave en Git.

`JWT_EXPIRATION` acepta una duración ISO-8601 y vale `PT1H` por defecto.

Para crear el administrador inicial, define `ADMIN_USERNAME` y `ADMIN_PASSWORD` antes de arrancar. Son opcionales, pero deben proporcionarse juntos. La contraseña debe tener al menos 10 caracteres y como máximo 72 bytes UTF-8. Si el administrador ya existe, no se modifica su contraseña. Un usuario normal existente nunca se promueve automáticamente.

La base H2 actual es en memoria: usuarios y datos se pierden al detener el proceso. Configura una base persistente para conservarlos.

## Endpoints

| Endpoint | Acceso |
| --- | --- |
| `POST /api/auth/register` | Público; siempre crea el rol `USER` |
| `POST /api/auth/login` | Público; devuelve `accessToken`, `tokenType`, `expiresAt` |
| `GET /api/**` | JWT válido |
| `/api/admin/**` | JWT con rol `ADMIN` |
| Otras rutas y métodos | Denegados |

Registro y login reciben JSON con `username` y `password`. El usuario admite de 3 a 50 letras ASCII, números, puntos, guiones y guiones bajos; se almacena en minúsculas. La contraseña admite de 10 a 72 caracteres, sin superar 72 bytes UTF-8 por el límite de BCrypt.

Envía el token como `Authorization: Bearer <accessToken>`. La API valida la firma HS256, el emisor `starwapi` y los tiempos del JWT. No utiliza sesiones, cookies de autenticación ni HTTP Basic. Los errores de seguridad devuelven `ApiError` en JSON: 401 para autenticación inválida y 403 para permisos insuficientes.

No hay refresh tokens ni revocación individual. El token sirve hasta su vencimiento aunque se cambie el rol o contraseña en la base; rota la clave para invalidar todos los tokens. Usa HTTPS fuera de desarrollo local.
