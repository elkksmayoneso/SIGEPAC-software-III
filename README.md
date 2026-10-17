# SIGEPAC: Sistema de Gestión de Pagos de Administración para Plaza Cívica
### Municipio de Pitalito, Huila - Implementación Integral en Java

Bienvenido a la implementación completa de **SIGEPAC**, diseñada bajo estándares rigurosos de arquitectura de software, principios **SOLID**, seguridad criptográfica y persistencia relacional aislada.

Este proyecto implementa el sistema **4 veces de manera completamente independiente**, utilizando lenguaje **Java (JDK 22)** con **nombres de paquetes, clases, variables, métodos y carpetas estrictamente en español**:

1. **Iteración 1: Arquitectura MVC** (Modelo - Vista - Controlador) `[Puerto 8001]`
2. **Iteración 2: Arquitectura en Capas** (Presentación - Lógica de Negocio - Acceso a Datos - Entidades) `[Puerto 8002]`
3. **Iteración 3: Arquitectura Orientada a Objetos** (Dominio Rico, Aggregate Roots y Value Objects) `[Puerto 8003]`
4. **Iteración 4: Arquitectura Hexagonal** (Puertos y Adaptadores / Clean Architecture) `[Puerto 8004]`

---

## 📋 Cumplimiento de Historias de Usuario (HU)

| Historia de Usuario | Descripción Funcional | Requerimientos | Criterio de Aceptación Validado |
| :--- | :--- | :--- | :--- |
| **HU-01: Inicio de sesión seguro** | Autenticación de Administradora y Propietarios. | RF10, RNF1, RNF3 | Valida credenciales contra algoritmo de hash SHA-256 con Salt. Emite mensaje de error claro ante datos incorrectos sin fuga de información. |
| **HU-02: Registro de nuevos usuarios** | Creación, listado, edición y baja de usuarios. | RF1, RF3, RF4 | Exige campos obligatorios (`nombre`, `apellido`, `correo`, `contraseña`, `rol`, `teléfono`). Rechaza contraseñas inseguras (<6 caracteres). |
| **HU-03: Registro de negocios** | Creación y administración de locales comerciales. | RF2, RF3, RF4 | Asigna código único de local (`LOC-XXX`) y vincula a un propietario existente previamente. Permite consulta, edición y baja. |
| **HU-04: Registro de deudas / alquileres** | Fijación y actualización del canon de arrendamiento. | RF5, RNF7 | Asocia la obligación económica a un negocio existente. Almacena de forma persistente el canon y el saldo deudor. |
| **HU-05: Registro de pagos y actualización inmediata** | Recaudación y liquidación de cuotas. | RF6, RNF5 | **Flujo estricto**: Exige buscar y verificar la existencia previa del negocio (error explícito si no existe). Actualiza el saldo pendiente **inmediatamente** y cambia el estado a `PAGADO` cuando el saldo llega a cero. |

---

## 🛡️ Requisitos No Funcionales (RNF)

* **RNF3 (Seguridad de la Información):** Ninguna contraseña se almacena en texto plano. Cada arquitectura implementa algoritmos criptográficos `SHA-256` con semilla secreta (*Salt*), protegiendo la información frente a accesos no autorizados o filtraciones.
* **RNF5 (Concurrencia e Integridad Transaccional):** Los flujos de pago usan transacciones JDBC de InnoDB, bloqueo de las deudas afectadas y rollback ante fallos para impedir que pagos y saldos queden desincronizados.
* **RNF7 (Persistencia y Bases de Datos Aisladas):** Cada una de las 4 iteraciones cuenta con su propio script de base de datos relacional independiente en SQL (`InnoDB`, claves foráneas, restricciones de chequeo) y almacén de persistencia aislado:
  * `1_mvc/sql/esquema_mvc.sql`
  * `2_capas/sql/esquema_capas.sql`
  * `3_orientada_objetos/sql/esquema_poo.sql`
  * `4_hexagonal/sql/esquema_hexagonal.sql`

---

## 🏛️ Comparativa y Principios SOLID por Arquitectura

### 1. Arquitectura MVC (`1_mvc/`)
* **Filosofía:** Separación clara entre datos de negocio y almacenamiento (`modelos`), flujo de control e interacción (`controladores`), y renderizado visual web (`servidor`).
* **SOLID:**
  * **SRP:** Cada modelo (`ModeloUsuario`, `ModeloNegocio`, `ModeloAlquiler`, `ModeloPago`) es el único responsable de operar sobre su tabla correspondiente.
  * **OCP:** Los controladores procesan solicitudes sin acoplarse al motor de base de datos específico.

### 2. Arquitectura en Capas (`2_capas/`)
* **Filosofía:** Flujo unidireccional estricto: `Presentación` ➡️ `Lógica de Negocio (BLL)` ➡️ `Acceso a Datos (DAL)` ➡️ `Base de Datos`.
* **SOLID:**
  * **DIP (Inversión de Dependencias):** Los servicios de la capa BLL (`ServicioPago`, `ServicioUsuario`) dependen exclusivamente de interfaces y contratos (`IRepositorioPago`, `IRepositorioUsuario`), desacoplándose por completo de la implementación técnica concreta de almacenamiento.
  * **ISP (Segregación de Interfaces):** Interfaces granulares y especializadas para cada repositorio.

### 3. Arquitectura Orientada a Objetos Rica (`3_orientada_objetos/`)
* **Filosofía:** Dominio Rico (*Rich Domain Model*) combatiendo el antipatrón de modelo anémico.
* **Componentes Clave:**
  * **Objetos de Valor (Value Objects):** `Dinero` (inmutable, redondeo a 2 decimales, sumas/restas seguras) y `CorreoElectronico` (inmutable con validación regex).
  * **Aggregate Root:** `DeudaAlquiler` encapsula su saldo y expone el método de negocio `registrarAbono(...)`, garantizando que ninguna regla de saldo se rompa externamente.
  * **Entidades con Comportamiento:** `CuentaUsuario.verificarContrasena(...)`.

### 4. Arquitectura Hexagonal (`4_hexagonal/`)
* **Filosofía:** Puertos y Adaptadores (*Alistair Cockburn*). El Núcleo de la aplicación (`nucleo/`) es 100% independiente del framework web, base de datos y detalles de infraestructura.
* **Componentes Clave:**
  * **Puertos de Entrada (Driving Ports):** `ICasoUsoIniciarSesion`, `ICasoUsoGestionarUsuario`, `ICasoUsoGestionarNegocio`, `ICasoUsoRegistrarDeuda`, `ICasoUsoProcesarPago`.
  * **Puertos de Salida (Driven Ports):** `IPuertoPersistenciaUsuario`, `IPuertoPersistenciaNegocio`, `IPuertoPersistenciaAlquiler`, `IPuertoPersistenciaPago`, `IPuertoCifradorContrasena`, `IPuertoTransaccion`.
  * **Adaptadores:** Controladores Web HTTP en entrada; Repositorios de persistencia y Cifrador SHA-256 en salida.

---

## 📂 Árbol de Directorios del Proyecto

```text
sigepac_java/
│
├── 1_mvc/                                    # ITERACIÓN 1: ARQUITECTURA MVC
│   ├── configuracion/                        # Configuración JDBC y seguridad
│   │   ├── ConexionMvc.java
│   │   └── CifradorContrasenaMvc.java
│   ├── controladores/                        # Controladores de flujo
│   │   ├── ControladorAutenticacion.java
│   │   ├── ControladorUsuario.java
│   │   ├── ControladorNegocio.java
│   │   ├── ControladorAlquiler.java
│   │   └── ControladorPago.java
│   ├── modelos/                              # Modelos de datos
│   │   ├── ModeloUsuario.java
│   │   ├── ModeloNegocio.java
│   │   ├── ModeloAlquiler.java
│   │   └── ModeloPago.java
│   ├── servidor/                             # Servidor HTTP embebido (Puerto 8001)
│   │   └── ServidorMvc.java
│   └── sql/                                  # DDL de Base de Datos Aislada
│       └── esquema_mvc.sql
│
├── 2_capas/                                  # ITERACIÓN 2: ARQUITECTURA EN CAPAS
│   ├── acceso_datos/                         # Capa DAL (Data Access Layer)
│   │   ├── contratos/                        # Interfaces de repositorios (ISP/DIP)
│   │   └── repositorios/                     # Implementación concreta DAL
│   ├── dominio/entidades/                    # Entidades de dominio transversal
│   ├── infraestructura/                      # Conexión JDBC y adaptadores de seguridad
│   │   ├── ConexionCapas.java
│   │   └── CifradorContrasenaCapas.java
│   ├── logica_negocio/                       # Capa BLL (Business Logic Layer)
│   │   ├── contratos/                        # Interfaces de servicios de negocio
│   │   └── servicios/                        # Reglas de negocio y orquestación
│   ├── presentacion/                         # Capa de Presentación
│   │   └── controladores/
│   ├── servidor/                             # Servidor HTTP embebido (Puerto 8002)
│   │   └── ServidorCapas.java
│   └── sql/                                  # DDL de Base de Datos Aislada
│       └── esquema_capas.sql
│
├── 3_orientada_objetos/                      # ITERACIÓN 3: ARQUITECTURA OO (DOMINIO RICO)
│   ├── aplicacion/manejadores/               # Coordinadores de casos de uso
│   ├── dominio/
│   │   ├── entidades/                        # Aggregate Roots y Entidades ricas
│   │   │   ├── CuentaUsuario.java
│   │   │   ├── DeudaAlquiler.java
│   │   │   ├── NegocioComercial.java
│   │   │   └── TransaccionPago.java
│   │   ├── excepciones/                      # Excepciones de dominio
│   │   ├── objetos_valor/                    # Value Objects inmutables (Dinero, Correo)
│   │   └── repositorios/                     # Contratos de persistencia de dominio
│   ├── infraestructura/                      # Adaptadores JDBC y de seguridad
│   │   ├── ConexionPoo.java
│   │   ├── AdaptadorCifradorPoo.java
│   │   └── persistencia/                     # Repositorios JDBC
│   ├── presentacion/controladores/
│   ├── servidor/                             # Servidor HTTP embebido (Puerto 8003)
│   │   └── ServidorPoo.java
│   └── sql/                                  # DDL de Base de Datos Aislada
│       └── esquema_poo.sql
│
├── 4_hexagonal/                              # ITERACIÓN 4: ARQUITECTURA HEXAGONAL
│   ├── adaptadores/
│   │   ├── entrada/web/                      # Driving Adapters (Controladores Web)
│   │   └── salida/                           # Driven Adapters
│   │       ├── persistencia/                 # Adaptadores JDBC y transacciones
│   │       └── seguridad/                    # Adaptador cifrador SHA-256
│   ├── nucleo/                               # Core Hexagonal Agnóstico
│   │   ├── aplicacion/servicios/             # Implementación de Casos de Uso
│   │   ├── dominio/                          # Modelos puros y excepciones
│   │   │   ├── excepciones/
│   │   │   └── modelos/
│   │   └── puertos/
│   │       ├── entrada/                      # Driving Ports (Interfaces de Casos de Uso)
│   │       └── salida/                       # Driven Ports (Interfaces hacia Infraestructura)
│   ├── servidor/                             # Servidor HTTP embebido (Puerto 8004)
│   │   └── ServidorHexagonal.java
│   └── sql/                                  # DDL de Base de Datos Aislada
│       └── esquema_hexagonal.sql
│
├── pruebas/                                  # BATERÍA DE PRUEBAS AUTOMATIZADAS
│   └── PruebaTodasLasArquitecturas.java      # 34 Pruebas Unitarias e Integrales
│
├── bin/                                      # Bytecode Java compilado (.class)
├── Lanzador.java                             # Menú interactivo y despachador de servicios
└── README.md                                 # Documentación técnica completa
```

---

## 🚀 Compilación y Ejecución

El proyecto usa Java SE (JDK 22), JDBC y MySQL Connector/J. Cada arquitectura tiene su propia base de datos y los modelos/repositorios persisten en las tablas de su esquema.

### Configuración de las bases de datos

1. Instale MySQL 8 o MariaDB y ejecute una vez los cuatro scripts `sql/esquema_*.sql` de sus carpetas. Cada script crea una base independiente.
2. Instale MySQL Connector/J y defina `SIGEPAC_JDBC_JAR` con la ruta completa al controlador `.jar`.
3. Configure usuario y contraseña como variables de entorno. Cada arquitectura admite URL JDBC y credenciales propias:

| Arquitectura | URL JDBC | Usuario | Contraseña |
| --- | --- | --- | --- |
| MVC | `SIGEPAC_MVC_JDBC_URL` | `SIGEPAC_MVC_DB_USER` | `SIGEPAC_MVC_DB_PASSWORD` |
| Capas | `SIGEPAC_CAPAS_JDBC_URL` | `SIGEPAC_CAPAS_DB_USER` | `SIGEPAC_CAPAS_DB_PASSWORD` |
| POO | `SIGEPAC_POO_JDBC_URL` | `SIGEPAC_POO_DB_USER` | `SIGEPAC_POO_DB_PASSWORD` |
| Hexagonal | `SIGEPAC_HEX_JDBC_URL` | `SIGEPAC_HEX_DB_USER` | `SIGEPAC_HEX_DB_PASSWORD` |

Las URLs predeterminadas apuntan a `localhost:3306` y a la base nombrada en cada esquema. El usuario predeterminado es `root` sin contraseña. En PowerShell, por ejemplo:

```powershell
$env:SIGEPAC_JDBC_JAR = "C:\ruta\mysql-connector-j.jar"
$env:SIGEPAC_MVC_DB_USER = "sigepac_mvc"
$env:SIGEPAC_MVC_DB_PASSWORD = "su-clave"
```

### 1. Compilación Completa
Desde la terminal PowerShell o CMD en la carpeta raíz `sigepac_java`:
```powershell
$fuentes = Get-ChildItem -Path "1_mvc","2_capas","3_orientada_objetos","4_hexagonal","pruebas","Lanzador.java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d "bin" $fuentes
```

### 2. Ejecución de la Suite de Pruebas Automatizadas
Para verificar el 100% del funcionamiento de las 4 arquitecturas y las 5 Historias de Usuario:
```powershell
java -cp "bin;$env:SIGEPAC_JDBC_JAR" sigepac.Lanzador pruebas
```
*Salida esperada:*
```text
================================================================================
  SUITE DE PRUEBAS INTEGRALES SIGEPAC - JAVA (PLAZA CIVICA DE PITALITO)
  Validacion de HU-01 a HU-05 y RNF1, RNF3, RNF5, RNF7 en 4 Arquitecturas
================================================================================
...
================================================================================
  RESUMEN GENERAL DE EJECUCION
  Total de pruebas ejecutadas: 34
  Pruebas SUPERADAS [PASS]  : 34
  Pruebas FALLIDAS  [FAIL]  : 0
================================================================================
  >>> RESULTADO: TODAS LAS PRUEBAS PASARON EXITOSAMENTE (100% OK) <<<
```

### 3. Iniciar Servidores Web

Puedes iniciar los servidores web usando el comando `Lanzador` con el nombre de la arquitectura deseada:

* **Iniciar Servidor MVC (Puerto 8001):**
  ```powershell
  java -cp "bin;$env:SIGEPAC_JDBC_JAR" sigepac.Lanzador mvc
  ```
  Acceso en el navegador: [http://localhost:8001](http://localhost:8001)

* **Iniciar Servidor En Capas (Puerto 8002):**
  ```powershell
  java -cp "bin;$env:SIGEPAC_JDBC_JAR" sigepac.Lanzador capas
  ```
  Acceso en el navegador: [http://localhost:8002](http://localhost:8002)

* **Iniciar Servidor Orientado a Objetos (Puerto 8003):**
  ```powershell
  java -cp "bin;$env:SIGEPAC_JDBC_JAR" sigepac.Lanzador poo
  ```
  Acceso en el navegador: [http://localhost:8003](http://localhost:8003)

* **Iniciar Servidor Hexagonal (Puerto 8004):**
  ```powershell
  java -cp "bin;$env:SIGEPAC_JDBC_JAR" sigepac.Lanzador hexagonal
  ```
  Acceso en el navegador: [http://localhost:8004](http://localhost:8004)

* **Iniciar TODAS las arquitecturas simultáneamente:**
  ```powershell
  java -cp "bin;$env:SIGEPAC_JDBC_JAR" sigepac.Lanzador todas
  ```

---

## 🔐 Credenciales de Acceso Inicial

* **Rol:** Administradora
* **Correo Electrónico:** `admin@sigepac.com`
* **Contraseña:** `Admin123*`
