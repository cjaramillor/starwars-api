# Postman

Importa `Starwapi.postman_collection.json` y selecciona un entorno:

- Para la aplicación iniciada en esta sesión: importa `../target/Starwapi.local.postman_environment.json`. Incluye las credenciales aleatorias del administrador creado al arrancar y de un usuario listo para registrar. Es un archivo local con contraseñas; no lo compartas.
- Para otro arranque o servidor: importa `Starwapi.postman_environment.json` y completa `baseUrl`, `username`, `password`, `adminUsername` y `adminPassword`.

Ejecuta las peticiones en este orden:

1. **Autenticación / Login administrador**: guarda `adminToken` en el entorno activo.
2. **Administración / Sincronizar SWAPI**: carga los datos. Puede tardar varios minutos; aumenta el timeout de Postman si hace falta. Requiere acceso desde la aplicación a SWAPI.
3. **Autenticación / Registrar usuario**: crea un usuario normal. Si ya existe, devuelve 409; continúa con el login.
4. **Autenticación / Login usuario**: guarda `token` en el entorno activo.
5. **Catálogo**: consulta personajes y relaciones. Cambia `characterId`, `filmId` y los filtros del entorno según necesites.

Si los tokens expiran, repite los logins. El catálogo empieza vacío; consultar un ID antes de sincronizar puede devolver 404. Los controladores actuales no exponen un listado ni un detalle individual de películas, por lo que esas rutas no se incluyen.

La base H2 es temporal: los usuarios y datos se pierden al detener la aplicación. Las credenciales del entorno de sesión corresponden únicamente al arranque preparado por Codex.

Los logs del proceso local están en `../target/app.stdout.log` y `../target/app.stderr.log`, y su PID está en `../target/app.pid`.

## Cambiar H2 por PostgreSQL

Estos pasos se ejecutan desde la raíz del proyecto (`starwapi`). Son instrucciones para un cambio futuro: la configuración actual sigue utilizando H2 en memoria. El ejemplo utiliza PostgreSQL; para otro motor hay que adaptar el driver, la URL JDBC y la creación de la base.

### 1. Preparar la base de datos

Instala o utiliza un servidor PostgreSQL accesible desde la aplicación. Conéctate mediante `psql` con un usuario que pueda crear roles y bases, y ejecuta lo siguiente fuera de una transacción:

```sql
CREATE ROLE starwapi_app LOGIN;
\password starwapi_app
CREATE DATABASE starwapi OWNER starwapi_app;
```

`\password` solicita la contraseña sin escribirla en el script. La contraseña de este rol sirve para la conexión JDBC; es distinta de las contraseñas de los usuarios de la API. Comprueba host, puerto y permisos de acceso antes de iniciar la aplicación.

### 2. Cambiar las dependencias de Maven

En `pom.xml`, agrega el driver de PostgreSQL; Spring Boot administra su versión:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

Si quieres dejar H2 solamente para pruebas, cambia el `scope` de `com.h2database:h2` de `runtime` a `test` y elimina la dependencia `org.springframework.boot:spring-boot-h2console`. Mantén las dependencias de Spring Data JPA. Maven necesitará acceso a su repositorio para descargar el driver la primera vez.

### 3. Crear un perfil de conexión

Crea `src/main/resources/application-postgres.properties` con este contenido, conservando las propiedades actuales de SWAPI y JWT en `application.properties`:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=${DB_DDL_AUTO:validate}
```

Spring Boot deduce el driver desde la URL JDBC. No hace falta fijar el dialecto de Hibernate para este caso. Las contraseñas se proporcionan mediante el entorno o un gestor de secretos.

### 4. Inicializar el esquema y arrancar

Para una base nueva de desarrollo, abre PowerShell y configura:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'postgres'
$env:DB_URL = 'jdbc:postgresql://localhost:5432/starwapi'
$env:DB_USERNAME = 'starwapi_app'
# Solicita la contraseña JDBC sin escribirla en el historial.
$dbCredential = Get-Credential -UserName 'starwapi_app' -Message 'Contraseña de PostgreSQL'
$env:DB_PASSWORD = $dbCredential.GetNetworkCredential().Password
$env:DB_DDL_AUTO = 'update'

# Si aún no tienes una clave JWT, genera una y consérvala para los siguientes arranques.
$jwtBytes = New-Object byte[] 32
$jwtRng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$jwtRng.GetBytes($jwtBytes)
$jwtRng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)

# Opcional: crear el administrador inicial en esta nueva base.
$adminCredential = Get-Credential -Message 'Administrador inicial de la API'
$env:ADMIN_USERNAME = $adminCredential.UserName
$env:ADMIN_PASSWORD = $adminCredential.GetNetworkCredential().Password

./mvnw.cmd spring-boot:run
```

El administrador debe cumplir las reglas de usuario y contraseña descritas en `../SECURITY.md`. Si ya configuraste una clave JWT, utiliza esa misma clave en lugar de generar otra. Detén antes el proceso que esté usando el puerto 8080.

`update` permite que Hibernate cree las tablas en esta base de desarrollo. Después del primer arranque correcto, detén la aplicación y cambia el modo antes de volver a iniciarla:

```powershell
$env:DB_DDL_AUTO = 'validate'
./mvnw.cmd spring-boot:run
```

`validate` comprueba el esquema existente y falla si no coincide con las entidades; no crea tablas. En producción, prepara y versiona el esquema mediante migraciones de Flyway o Liquibase y usa `validate`. El proyecto todavía no incluye esas migraciones. Evita `create` y `create-drop` sobre una base con datos que quieras conservar.

### 5. Mantener H2 aislado para las pruebas

Si conservas H2 con `scope=test`, agrega estas propiedades a `src/test/resources/application.properties`, manteniendo las propiedades de prueba de JWT y SWAPI:

```properties
spring.datasource.url=jdbc:h2:mem:starwapi-test;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
```

Ejecuta las pruebas sin el perfil PostgreSQL activo, por ejemplo desde otra terminal sin `SPRING_PROFILES_ACTIVE`, o quitándolo de la terminal actual antes de `./mvnw.cmd test`. La base indicada en esta sección es exclusivamente de pruebas y sus tablas se recrean.

### 6. Verificar desde Postman

Comprueba en los logs que la conexión JDBC apunta a PostgreSQL y que el perfil `postgres` está activo. Después:

1. Actualiza las credenciales del entorno Postman para que coincidan con las de esta base.
2. Ejecuta los logins para obtener tokens nuevos; registra el usuario normal si todavía no existe.
3. Ejecuta **Sincronizar SWAPI** y consulta el catálogo.
4. Reinicia la aplicación con la misma configuración y consulta de nuevo: usuarios y datos deben seguir presentes.

El contenido de la H2 en memoria no se copia automáticamente a PostgreSQL. Para el catálogo puedes volver a sincronizar; los usuarios anteriores requieren una migración explícita o un nuevo registro. La colección y las rutas HTTP no cambian.

Referencias: [conexiones SQL en Spring Boot](https://docs.spring.io/spring-boot/reference/data/sql.html), [inicialización del esquema](https://docs.spring.io/spring-boot/how-to/data-initialization.html) y [creación de bases en PostgreSQL](https://www.postgresql.org/docs/17/manage-ag-createdb.html).
