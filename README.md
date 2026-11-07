# SleepTrack

Aplicación móvil nativa para Android desarrollada en Java con backend en PHP y base de datos MySQL, orientada al registro, seguimiento y análisis de los hábitos de sueño y descanso de estudiantes universitarios.

## Características principales

- **Autenticación y perfil de usuario:** Registro con datos académicos, inicio de sesión con contraseñas encriptadas con BCrypt y recuperación de cuenta.
- **Configuración de descanso:** Personalización de horario objetivo para dormir y despertar, así como meta diaria de horas de sueño.
- **Monitoreo con sensores:** Detección de movimientos nocturnos mediante el acelerómetro del dispositivo móvil, cálculo de duración real, eficiencia y calidad del descanso.
- **Registro diario de hábitos:** Encuesta matutina sobre calidad percibida, nivel de energía, exposición a pantallas antes de dormir, consumo de cafeína y niveles de estrés.
- **Dashboard y métricas:** Resumen del descanso reciente, desglose de horarios y cumplimiento de metas.
- **Análisis semanal e historial mensual:** Gráficos comparativos entre días de semana y fines de semana, calendario mensual interactivo con indicadores diarios de descanso.
- **Recomendaciones de higiene del sueño:** Consejos clasificados por categorías (horarios, uso de pantallas, manejo del estrés y consumo de cafeína).
- **Gestión de privacidad:** Opciones para eliminar registros históricos o la cuenta de usuario de forma segura.

## Tecnologías utilizadas

- **Android:** Java 17, Android SDK, Material Design Components, Volley, Gson.
- **Backend:** PHP 8.x con arquitectura REST / JSON.
- **Base de Datos:** MySQL / MariaDB (motor InnoDB, claves foráneas e integridad referencial).
- **Servidor local:** XAMPP (Apache y MySQL).

## Estructura del proyecto

```text
SLEEPTRACKAPP/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/sleeptrackapp/   # Clases Java (Activities, helpers, utilidades)
│   │   │   └── res/                              # Layouts XML, drawables, valores de colores y estilos
│   │   └── test/                                 # Pruebas unitarias
│   └── build.gradle.kts                          # Configuración Gradle del módulo app
├── api/                                          # Endpoints del backend en PHP
│   ├── db_connect.php                            # Conexión a MySQL y creación de tablas
│   ├── login.php                                 # Autenticación de usuarios
│   ├── register.php                              # Registro de cuentas
│   ├── configurar.php                            # Guardado de metas y recordatorios
│   ├── guardar_monitoreo.php                     # Persistencia de datos del acelerómetro
│   ├── guardar_registro.php                      # Guardado de encuesta diaria
│   ├── obtener_resumen.php                       # Consulta de datos del dashboard
│   ├── obtener_analisis_semanal.php              # Métricas semanales
│   ├── obtener_historial.php                     # Consulta de historial mensual
│   ├── obtener_perfil.php                        # Datos del perfil de usuario
│   ├── eliminar_datos.php                        # Eliminación de historial o cuenta
│   └── recuperar.php                             # Restablecimiento de contraseña
├── database.sql                                  # Script SQL con estructura de tablas y catálogo inicial
├── seed_data.sql                                 # Datos de prueba para evaluación
└── build.gradle.kts                              # Configuración raíz de Gradle
```

## Requisitos previos

- Android Studio (versión Jellyfish / Koala o superior recomendada).
- JDK 17 o superior.
- Dispositivo físico con Android 14+ (API 34+) o emulador configurado.
- XAMPP instalado con Apache y MySQL activos.

## Instalación y configuración

### 1. Configuración del Backend y Base de Datos

1. Iniciar los servicios de **Apache** y **MySQL** desde el panel de control de XAMPP.
2. Copiar la carpeta del proyecto dentro del directorio `htdocs` de XAMPP:
   ```text
   C:\xampp\htdocs\SLEEPTRACKAPP
   ```
3. Abrir phpMyAdmin en el navegador (`http://localhost/phpmyadmin`).
4. Crear la base de datos `sleeptrack` o importar directamente el archivo `database.sql`.
5. *(Opcional)* Si se desean cargar datos de prueba para historial y análisis gráfico, importar el archivo `seed_data.sql`.
6. Verificar las credenciales de conexión en `api/db_connect.php` (por defecto: `localhost`, usuario `root`, sin contraseña).

### 2. Configuración de la Aplicación Android

1. Abrir Android Studio y seleccionar **Open** para abrir la carpeta `SLEEPTRACKAPP`.
2. Esperar a que Gradle sincronice las dependencias del proyecto.
3. Configurar la dirección IP de las peticiones:
   - Identificar la dirección IP local de la computadora (ejecutando `ipconfig` en Windows).
   - Asegurarse de que la IP en las URLs de las Activities apunte a la IP de la máquina donde se ejecuta XAMPP (por ejemplo, `http://192.168.X.X/SLEEPTRACKAPP/api/...`).
4. Conectar el dispositivo Android en modo depuración USB (o iniciar un emulador).
5. Ejecutar la aplicación desde Android Studio presionando **Run 'app'**.
