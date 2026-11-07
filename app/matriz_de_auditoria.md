# 📋 Matriz de Auditoría y Agrupación de Historias de Usuario (HU-01 a HU-23) - SleepTrack

Esta matriz documenta de forma exhaustiva la trazabilidad técnica, funcional y de diseño UX/UI de las **23 Historias de Usuario oficiales** de la aplicación **SleepTrack**, inspirada en los principios de diseño de **Apple (Human Interface Guidelines)** y en el algoritmo de monitoreo de **Sleep Cycle**.

---

## 🏗️ 1. Mapeo Oficial de Pantallas y Funcionalidades

| ID | Historia de Usuario | Pantalla Layout XML | Clase Java Principal | Endpoint API / Backend | Base de Datos (MySQL) | Estado y Verificación |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **HU-01** | Conocer SleepTrack y navegar a Registro/Login | `activity_hu01_bienvenida.xml` | `BienvenidaActivity.java` | N/A (Sesión local) | `SharedPreferences` | **100% Funcional** (Auto-login si ya existe sesión activa) |
| **HU-02** | Registro de datos personales, académicos y credenciales | `activity_hu02_crear_perfil.xml` | `CrearPerfilActivity.java` | `api/register.php` | `cuentas` | **100% Funcional** (BCrypt hashing, validación interactiva, Selectores de Fecha/Universidad/Carrera) |
| **HU-03** | Autenticación e Inicio de Sesión | `activity_hu03_iniciar_sesion.xml` | `IniciarSesionActivity.java` | `api/login.php` | `cuentas` | **100% Funcional** (Verificación hash BCrypt, guardado seguro de sesión en `SharedPreferences`) |
| **HU-04** | Recuperación de contraseña por Correo y Fecha Nac. | `dialog_hu04_recuperar_password.xml` | `IniciarSesionActivity.java` | `api/recuperar.php` | `cuentas` | **100% Funcional** (Modal Apple HIG, DatePickerDialog, reseteo de hash) |
| **HU-05** | Consentimiento de Sensores y Almacenamiento | `activity_hu05_consentimiento_monitoreo.xml` | `ConsentimientoActivity.java` | `api/configurar.php` | `cuentas (sensores_aceptados)` | **100% Funcional** (Explicación de privacidad HealthKit-style) |
| **HU-06** | Configuración de horarios objetivo y recordatorios | `activity_hu06_configurar_descanso.xml` | `ConfigurarDescansoActivity.java` | `api/configurar.php` | `cuentas (hora_dormir, hora_despertar, meta_horas)` | **100% Funcional** (TimePickerDialog interactivo, cálculo instantáneo de horas) |
| **HU-07** | Iniciar monitoreo nocturno con Acelerómetro | `activity_hu07_hu08_monitoreo.xml` | `MonitoreoActivity.java` | N/A (`SensorManager`) | `Sensor.TYPE_ACCELEROMETER` | **100% Funcional** (Dark mode Apple Sleep, anillo pulsante, filtro de movimiento) |
| **HU-08** | Cronómetro en vivo, contador de movimientos y finalizar | `activity_hu07_hu08_monitoreo.xml` | `MonitoreoActivity.java` | `api/guardar_monitoreo.php` | `registros_diarios` | **100% Funcional** (Detección $\Delta G > 2.2 m/s^2$, cálculo de duración, eficiencia y calidad, sincronización con backend) |
| **HU-09** | Encuesta de calidad percibida y energía por emojis | `activity_hu09_hu10_hu11_registro_diario.xml` | `RegistroDiarioActivity.java` | `api/guardar_registro.php` | `registros_diarios (calidad, energia)` | **100% Funcional** (Emojis dinámicos `😫`-`😁` y `🪫`-`🚀`) |
| **HU-10** | Factores contextuales: estrés, cafeína y pantallas | `activity_hu09_hu10_hu11_registro_diario.xml` | `RegistroDiarioActivity.java` | `api/guardar_registro.php` | `registros_diarios (estres, cafeina, pantallas)` | **100% Funcional** (Radio botones con marcadores dinámicos `🔘/⚪`) |
| **HU-11** | Guardado de registros en base de datos remota y local | `activity_hu09_hu10_hu11_registro_diario.xml` | `RegistroDiarioActivity.java` | `api/guardar_registro.php` | `registros_diarios` (`ON DUPLICATE KEY UPDATE`) | **100% Funcional** (Persistencia idempotente) |
| **HU-12** | Resumen del descanso, calidad y eficiencia | `activity_hu12_hu13_hu14_resumen_descanso.xml` | `ResumenDescansoActivity.java` | `api/obtener_resumen.php` | `registros_diarios` | **100% Funcional** (Hero Card con % de cumplimiento, eficiencia calculada, tarjeta de advertencia de deuda de sueño) |
| **HU-13** | Línea de tiempo nocturna segmentada | `activity_hu12_hu13_hu14_resumen_descanso.xml` | `ResumenDescansoActivity.java` | `api/obtener_resumen.php` | `registros_diarios (hora_inicio, hora_fin)` | **100% Funcional** (Horarios exactos de inicio y fin, interacciones y microdespertares) |
| **HU-14** | Recomendaciones educativas vinculadas | `activity_hu12_hu13_hu14_resumen_descanso.xml` | `ResumenDescansoActivity.java` | Acceso a HU-19/HU-20 | N/A | **100% Funcional** (Acceso directo a consejos contextuales) |
| **HU-15** | Resumen semanal con gráficos de barras interactivos | `activity_hu15_hu16_analisis_semanal.xml` | `AnalisisSemanalActivity.java` | `api/obtener_analisis_semanal.php` | `registros_diarios` | **100% Funcional** (Gráfico 7 barras Lun-Dom, alturas proporcionales, tintes de cumplimiento verde menta/morado) |
| **HU-16** | Comparativa de hábitos semana vs. fin de semana | `activity_hu15_hu16_analisis_semanal.xml` | `AnalisisSemanalActivity.java` | `api/obtener_analisis_semanal.php` | `registros_diarios` | **100% Funcional** (Detección de irregularidad > 1h con badge de advertencia) |
| **HU-17** | Calendario mensual interactivo con indicadores | `activity_hu17_hu18_historial_mensual.xml` | `HistorialMensualActivity.java` | `api/obtener_historial.php` | `registros_diarios` | **100% Funcional** (Cuadrícula interactiva, puntos de color por día, visor de detalle al pulsar cualquier día) |
| **HU-18** | Estadísticas mensuales acumuladas | `activity_hu17_hu18_historial_mensual.xml` | `HistorialMensualActivity.java` | `api/obtener_historial.php` | `registros_diarios` | **100% Funcional** (Días de mala calidad reportados, noches de uso alto de smartphone) |
| **HU-19** | Consejos de higiene filtrables por categoría | `activity_hu19_hu20_recomendaciones.xml` | `RecomendacionesActivity.java` | N/A (Guía clínica adaptativa) | N/A | **100% Funcional** (Chips de filtrado: Todos, Horarios, Pantallas, Estrés, Cafeína) |
| **HU-20** | Alertas de privación de sueño y logros semanales | `activity_hu19_hu20_recomendaciones.xml` | `RecomendacionesActivity.java` | `api/obtener_resumen.php` | `registros_diarios` | **100% Funcional** (Tarjeta de alerta preventiva 🚨 y tarjeta de racha de logro 🏆) |
| **HU-21** | Consultar perfil y credenciales académicas | `activity_hu21_hu22_perfil_cuenta.xml` | `PerfilCuentaActivity.java` | `api/login.php` / Cache local | `cuentas` | **100% Funcional** (Visualización de estudiante, universidad, carrera y accesos) |
| **HU-22** | Cerrar sesión con confirmación | `dialog_hu22_cerrar_sesion.xml` | `PerfilCuentaActivity.java` | N/A (Limpieza local) | `SharedPreferences` | **100% Funcional** (Modal Apple HIG, borrado seguro de tokens y redirección a login) |
| **HU-23** | Gestión de privacidad y eliminación de registros | `activity_hu23_privacidad_datos.xml`<br>`dialog_hu23_eliminar_registros.xml` | `PrivacidadDatosActivity.java` | `api/eliminar_datos.php` | `registros_diarios` | **100% Funcional** (Purga en MySQL y limpieza de caché local con confirmación) |

---

## 🎨 2. Principios de Apple Human Interface Guidelines (HIG) Aplicados

1. **Claridad y Jerarquía Visual**:
   - Tarjetas suaves elevadas con bordes redondeados (`16dp`-`24dp`).
   - Sombras tenues y sin bordes toscos (`elevation` de `4dp` a `8dp`).
   - Paleta de colores armónica: Morado SleepTrack (`#5B55FE`), Verde Menta de Éxito (`#00B37E`), Ámbar de Advertencia (`#FF9800`) y Rosa/Rojo de Alerta (`#E83D55`).
2. **Interacciones Táctiles y Micro-Feedback**:
   - Píldora indicadora en la barra de navegación inferior (`BottomNavigationView`) con transición fluida sin parpadeos (`overridePendingTransition(0, 0)`).
   - Encuestas con selección reactiva mediante emojis e íconos radio dinámicos (`🔘 / ⚪`).
   - Anillo de halo pulsante animado en la pantalla de monitoreo nocturno (`MonitoreoActivity`).
   - Gráfico de barras interactivo con feedback Toast al tocar cada día.
3. **Respeto a la Privacidad y Transparencia**:
   - Explicación de cómo y cuándo se accede al acelerómetro.
   - Opción explícita de eliminación de registros tanto localmente como en la base de datos remota.

---

## 🗄️ 3. Arquitectura de Base de Datos y Seguridad Backend

1. **Prepared Statements (MySQLi)**:
   - Todos los endpoints (`register.php`, `login.php`, `configurar.php`, `recuperar.php`, `guardar_monitoreo.php`, `guardar_registro.php`, `obtener_resumen.php`, `obtener_historial.php`, `obtener_analisis_semanal.php`, `eliminar_datos.php`) utilizan sentencias preparadas contra inyección SQL.
2. **Cifrado de Contraseñas**:
   - Algoritmo estándar BCrypt (`PASSWORD_BCRYPT`) mediante `password_hash()` y `password_verify()`.
3. **Autogestión y Migración Dinámica**:
   - `db_connect.php` asegura la creación de tablas e índices (`idx_correo`, `idx_correo_fecha`) e incorpora columnas nuevas sin interrumpir la operación existente.
