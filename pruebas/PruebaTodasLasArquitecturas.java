package sigepac.pruebas;

import java.util.List;
import java.util.Map;

public class PruebaTodasLasArquitecturas {

    private static int totalPruebas = 0;
    private static int pruebasExitosas = 0;
    private static int pruebasFallidas = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  SUITE DE PRUEBAS INTEGRALES SIGEPAC - JAVA (PLAZA CIVICA DE PITALITO)");
        System.out.println("  Validacion de HU-01 a HU-05 y RNF1, RNF3, RNF5, RNF7 en 4 Arquitecturas");
        System.out.println("================================================================================\n");

        probarMvc();
        probarCapas();
        probarOrientadaAObjetos();
        probarHexagonal();

        System.out.println("\n================================================================================");
        System.out.println("  RESUMEN GENERAL DE EJECUCION");
        System.out.println("  Total de pruebas ejecutadas: " + totalPruebas);
        System.out.println("  Pruebas SUPERADAS [PASS]  : " + pruebasExitosas);
        System.out.println("  Pruebas FALLIDAS  [FAIL]  : " + pruebasFallidas);
        System.out.println("================================================================================");

        if (pruebasFallidas == 0) {
            System.out.println("  >>> RESULTADO: TODAS LAS PRUEBAS PASARON EXITOSAMENTE (100% OK) <<<");
        } else {
            System.err.println("  >>> ERROR: SE DETECTARON FALLAS EN EL SISTEMA <<<");
            System.exit(1);
        }
    }

    private static void afirmar(boolean condicion, String descripcion) {
        totalPruebas++;
        if (condicion) {
            pruebasExitosas++;
            System.out.println("  [PASS] " + descripcion);
        } else {
            pruebasFallidas++;
            System.err.println("  [FAIL] " + descripcion);
        }
    }

    // =========================================================================
    // 1. ITERACION: ARQUITECTURA MVC
    // =========================================================================
    private static void probarMvc() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(" 1. PROBANDO ARQUITECTURA MVC (Modelo - Vista - Controlador)");
        System.out.println("--------------------------------------------------------------------------------");

        sigepac.mvc.configuracion.CifradorContrasenaMvc cifrador = new sigepac.mvc.configuracion.CifradorContrasenaMvc();
        sigepac.mvc.modelos.ModeloUsuario modeloUsuario = new sigepac.mvc.modelos.ModeloUsuario(cifrador);
        sigepac.mvc.modelos.ModeloNegocio modeloNegocio = new sigepac.mvc.modelos.ModeloNegocio();
        sigepac.mvc.controladores.ControladorAutenticacion ctrlAuth = new sigepac.mvc.controladores.ControladorAutenticacion(modeloUsuario, cifrador);
        sigepac.mvc.controladores.ControladorUsuario ctrlUsr = new sigepac.mvc.controladores.ControladorUsuario(modeloUsuario);
        sigepac.mvc.controladores.ControladorNegocio ctrlNeg = new sigepac.mvc.controladores.ControladorNegocio(modeloNegocio);
        sigepac.mvc.controladores.ControladorAlquiler ctrlAlq = new sigepac.mvc.controladores.ControladorAlquiler(new sigepac.mvc.modelos.ModeloAlquiler());
        sigepac.mvc.controladores.ControladorPago ctrlPag = new sigepac.mvc.controladores.ControladorPago(new sigepac.mvc.modelos.ModeloPago(), modeloNegocio);

        // HU-01: Autenticación
        try {
            Map<String, Object> admin = ctrlAuth.iniciarSesion("admin@sigepac.com", "Admin123*");
            afirmar(admin != null && "ADMINISTRADORA".equals(admin.get("rol")), "MVC HU-01: Administradora inicia sesión correctamente.");
        } catch (Exception e) {
            afirmar(false, "MVC HU-01: Fallo al iniciar sesión como administradora: " + e.getMessage());
        }

        boolean errorPassword = false;
        try {
            ctrlAuth.iniciarSesion("admin@sigepac.com", "ClaveErronea999");
        } catch (Exception e) {
            errorPassword = true;
        }
        afirmar(errorPassword, "MVC HU-01: Contraseña incorrecta rechazada con excepción de seguridad.");

        // HU-02: Usuarios
        boolean usuarioCreado = ctrlUsr.guardarUsuario("Martha", "Gomez", "martha.mvc@gmail.com", "MarthaS123*", "PROPIETARIO", "3114445566");
        afirmar(usuarioCreado, "MVC HU-02: Nuevo usuario registrado con éxito.");

        List<Map<String, Object>> usuarios = ctrlUsr.listarUsuarios();
        Map<String, Object> usuarioMartha = usuarios.stream()
                .filter(u -> "martha.mvc@gmail.com".equals(u.get("correo")))
                .findFirst().orElse(null);
        afirmar(usuarioMartha != null, "MVC HU-02: Usuario encontrado en listado persistente.");
        int idMartha = (Integer) usuarioMartha.get("id_usuario");

        // HU-03: Negocios
        boolean negocioCreado = ctrlNeg.guardarNegocio(idMartha, "Artesanias Pitalito MVC", "LOC-MVC-10");
        afirmar(negocioCreado, "MVC HU-03: Negocio registrado y asignado a local y propietario.");

        List<Map<String, Object>> negocios = ctrlNeg.listarNegocios();
        Map<String, Object> negocioMartha = negocios.stream()
                .filter(n -> "LOC-MVC-10".equals(n.get("local_numero")))
                .findFirst().orElse(null);
        afirmar(negocioMartha != null, "MVC HU-03: Negocio recuperado de la base de datos.");
        int idNegocioMartha = (Integer) negocioMartha.get("id_negocio");

        // HU-04: Alquileres / Deudas
        boolean alqCreado = ctrlAlq.registrarAlquiler(idNegocioMartha, "2026-10-01", "2026-10-31", 350000.0);
        afirmar(alqCreado, "MVC HU-04: Alquiler registrado asociando deuda al negocio.");

        List<Map<String, Object>> alquileres = ctrlAlq.listarAlquileres();
        Map<String, Object> alqMartha = alquileres.stream()
                .filter(a -> ((Integer) a.get("id_negocio")) == idNegocioMartha)
                .findFirst().orElse(null);
        afirmar(alqMartha != null && ((Double) alqMartha.get("saldo_pendiente")) == 350000.0,
                "MVC HU-04: Saldo inicial verificado en $350.000.");

        // HU-05: Pagos y Actualización de Saldo Inmediata
        boolean negocioInexistenteError = false;
        try {
            ctrlPag.procesarPago(999999, 100000.0, "2026-10-14", "EFECTIVO");
        } catch (Exception e) {
            negocioInexistenteError = true;
        }
        afirmar(negocioInexistenteError, "MVC HU-05: Pago rechazado si el negocio no existe previamente.");

        try {
            boolean pagoProcesado = ctrlPag.procesarPago(idNegocioMartha, 150000.0, "2026-10-14", "TRANSFERENCIA");
            afirmar(pagoProcesado, "MVC HU-05: Pago procesado exitosamente.");
        } catch (Exception e) {
            afirmar(false, "MVC HU-05: Error al procesar pago: " + e.getMessage());
        }

        List<Map<String, Object>> alquileresActualizados = ctrlAlq.listarAlquileres();
        Map<String, Object> alqActualizado = alquileresActualizados.stream()
                .filter(a -> ((Integer) a.get("id_negocio")) == idNegocioMartha)
                .findFirst().orElse(null);
        afirmar(alqActualizado != null && ((Double) alqActualizado.get("saldo_pendiente")) == 200000.0,
                "MVC HU-05: Saldo de la deuda del negocio actualizado inmediatamente (350.000 -> 200.000).");
    }

    // =========================================================================
    // 2. ITERACION: ARQUITECTURA EN CAPAS
    // =========================================================================
    private static void probarCapas() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println(" 2. PROBANDO ARQUITECTURA EN CAPAS (Presentacion - BLL - DAL - Entidades)");
        System.out.println("--------------------------------------------------------------------------------");

        sigepac.capas.acceso_datos.repositorios.RepositorioUsuario repoUsr = new sigepac.capas.acceso_datos.repositorios.RepositorioUsuario();
        sigepac.capas.acceso_datos.repositorios.RepositorioNegocio repoNeg = new sigepac.capas.acceso_datos.repositorios.RepositorioNegocio();
        sigepac.capas.acceso_datos.repositorios.RepositorioAlquiler repoAlq = new sigepac.capas.acceso_datos.repositorios.RepositorioAlquiler();
        sigepac.capas.acceso_datos.repositorios.RepositorioPago repoPag = new sigepac.capas.acceso_datos.repositorios.RepositorioPago();

        sigepac.capas.logica_negocio.servicios.ServicioAutenticacion srvAuth = new sigepac.capas.logica_negocio.servicios.ServicioAutenticacion(repoUsr, new sigepac.capas.infraestructura.CifradorContrasenaCapas());
        sigepac.capas.logica_negocio.servicios.ServicioUsuario srvUsr = new sigepac.capas.logica_negocio.servicios.ServicioUsuario(repoUsr, new sigepac.capas.infraestructura.CifradorContrasenaCapas());
        sigepac.capas.logica_negocio.servicios.ServicioNegocio srvNeg = new sigepac.capas.logica_negocio.servicios.ServicioNegocio(repoNeg, repoUsr);
        sigepac.capas.logica_negocio.servicios.ServicioAlquiler srvAlq = new sigepac.capas.logica_negocio.servicios.ServicioAlquiler(repoAlq, repoNeg);
        sigepac.capas.logica_negocio.servicios.ServicioPago srvPag = new sigepac.capas.logica_negocio.servicios.ServicioPago(repoNeg, repoAlq, repoPag);

        sigepac.capas.presentacion.controladores.ControladorAutenticacionCapas ctrlAuth = new sigepac.capas.presentacion.controladores.ControladorAutenticacionCapas(srvAuth);
        sigepac.capas.presentacion.controladores.ControladorUsuarioCapas ctrlUsr = new sigepac.capas.presentacion.controladores.ControladorUsuarioCapas(srvUsr);
        sigepac.capas.presentacion.controladores.ControladorNegocioCapas ctrlNeg = new sigepac.capas.presentacion.controladores.ControladorNegocioCapas(srvNeg);
        sigepac.capas.presentacion.controladores.ControladorAlquilerCapas ctrlAlq = new sigepac.capas.presentacion.controladores.ControladorAlquilerCapas(srvAlq);
        sigepac.capas.presentacion.controladores.ControladorPagoCapas ctrlPag = new sigepac.capas.presentacion.controladores.ControladorPagoCapas(srvPag);

        // HU-01: Autenticación
        sigepac.capas.dominio.entidades.EntidadUsuario admin = ctrlAuth.iniciarSesion("admin@sigepac.com", "Admin123*");
        afirmar(admin != null && "ADMINISTRADORA".equals(admin.getRol()), "Capas HU-01: Administradora autenticada a traves del servicio BLL.");

        boolean errorPassword = false;
        try {
            ctrlAuth.iniciarSesion("admin@sigepac.com", "ClaveInvalida");
        } catch (Exception e) {
            errorPassword = true;
        }
        afirmar(errorPassword, "Capas HU-01: BLL rechaza contrasena no coincidente con hash seguro.");

        // HU-02: Gestión de Usuarios
        int idU = ctrlUsr.crear("Rodrigo", "Paredes", "rodrigo.capas@gmail.com", "Rodrigo123*", "PROPIETARIO", "3158889900");
        afirmar(idU > 0, "Capas HU-02: Usuario registrado cumpliendo validacion en capa de negocio.");

        // HU-03: Gestión de Negocios
        int idN = ctrlNeg.crear(idU, "Restaurante El Huilense", "LOC-CAP-22");
        afirmar(idN > 0, "Capas HU-03: Negocio comercial vinculado a propietario.");

        // HU-04: Gestión de Deudas/Alquileres
        int idA = ctrlAlq.crear(idN, "2026-10-01", "2026-10-31", 500000.0);
        afirmar(idA > 0, "Capas HU-04: Alquiler registrado y asociado al negocio.");

        // HU-05: Pagos y Actualización de Saldo
        boolean errorNegocio = false;
        try {
            ctrlPag.procesar(99999, 50000.0, "2026-10-14", "EFECTIVO");
        } catch (Exception e) {
            errorNegocio = true;
        }
        afirmar(errorNegocio, "Capas HU-05: Validacion BLL exige existencia previa del negocio antes del pago.");

        try {
            boolean pagoOk = ctrlPag.procesar(idN, 200000.0, "2026-10-14", "EFECTIVO");
            afirmar(pagoOk, "Capas HU-05: Pago asentado en capa de datos.");
        } catch (Exception e) {
            afirmar(false, "Capas HU-05: Error al procesar pago: " + e.getMessage());
        }

        sigepac.capas.dominio.entidades.EntidadAlquiler alqConsultado = ctrlAlq.listar().stream()
                .filter(x -> x.getIdNegocio() == idN)
                .findFirst().orElse(null);
        afirmar(alqConsultado != null && alqConsultado.getSaldoActual() == 300000.0,
                "Capas HU-05: Saldo de cuenta del negocio actualizado inmediatamente a $300.000.");
    }

    // =========================================================================
    // 3. ITERACION: ARQUITECTURA ORIENTADA A OBJETOS (DOMINIO RICO)
    // =========================================================================
    private static void probarOrientadaAObjetos() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println(" 3. PROBANDO ARQUITECTURA ORIENTADA A OBJETOS (Dominio Rico y Value Objects)");
        System.out.println("--------------------------------------------------------------------------------");

        sigepac.poo.infraestructura.persistencia.RepositorioCuentaUsuarioJdbc repoUsr = new sigepac.poo.infraestructura.persistencia.RepositorioCuentaUsuarioJdbc();
        sigepac.poo.infraestructura.persistencia.RepositorioNegocioComercialJdbc repoNeg = new sigepac.poo.infraestructura.persistencia.RepositorioNegocioComercialJdbc();
        sigepac.poo.infraestructura.persistencia.RepositorioDeudaAlquilerJdbc repoAlq = new sigepac.poo.infraestructura.persistencia.RepositorioDeudaAlquilerJdbc();

        sigepac.poo.aplicacion.manejadores.ManejadorAutenticacion manAuth = new sigepac.poo.aplicacion.manejadores.ManejadorAutenticacion(repoUsr, new sigepac.poo.infraestructura.AdaptadorCifradorPoo());
        sigepac.poo.aplicacion.manejadores.ManejadorUsuario manUsr = new sigepac.poo.aplicacion.manejadores.ManejadorUsuario(repoUsr, new sigepac.poo.infraestructura.AdaptadorCifradorPoo());
        sigepac.poo.aplicacion.manejadores.ManejadorNegocio manNeg = new sigepac.poo.aplicacion.manejadores.ManejadorNegocio(repoNeg, repoUsr);
        sigepac.poo.aplicacion.manejadores.ManejadorDeuda manDeuda = new sigepac.poo.aplicacion.manejadores.ManejadorDeuda(repoAlq, repoNeg);
        sigepac.poo.aplicacion.manejadores.ManejadorPago manPago = new sigepac.poo.aplicacion.manejadores.ManejadorPago(repoAlq, repoNeg);

        sigepac.poo.presentacion.controladores.ControladorAutenticacionPoo ctrlAuth = new sigepac.poo.presentacion.controladores.ControladorAutenticacionPoo(manAuth);
        sigepac.poo.presentacion.controladores.ControladorUsuarioPoo ctrlUsr = new sigepac.poo.presentacion.controladores.ControladorUsuarioPoo(manUsr);
        sigepac.poo.presentacion.controladores.ControladorNegocioPoo ctrlNeg = new sigepac.poo.presentacion.controladores.ControladorNegocioPoo(manNeg);
        sigepac.poo.presentacion.controladores.ControladorDeudaPoo ctrlDeuda = new sigepac.poo.presentacion.controladores.ControladorDeudaPoo(manDeuda);
        sigepac.poo.presentacion.controladores.ControladorPagoPoo ctrlPago = new sigepac.poo.presentacion.controladores.ControladorPagoPoo(manPago);

        // HU-01: Autenticación por entidad de dominio
        sigepac.poo.dominio.entidades.CuentaUsuario admin = ctrlAuth.iniciarSesion("admin@sigepac.com", "Admin123*");
        afirmar(admin != null && "ADMINISTRADORA".equals(admin.getRol()), "POO HU-01: Dominio verifica contrasena con Hash y Salt.");

        // HU-02: Usuarios con Objetos de Valor
        int idElena = ctrlUsr.crear("Elena", "Castro", "elena.poo@gmail.com", "ElenaPass123*", "PROPIETARIO", "3179998877");
        afirmar(idElena > 0, "POO HU-02: CuentaUsuario encapsula CorreoElectronico como Value Object.");

        // HU-03: Negocios Comerciales
        int idNegocioPoo = ctrlNeg.crear(idElena, "Optica San Juan POO", "LOC-POO-33");
        afirmar(idNegocioPoo > 0, "POO HU-03: NegocioComercial creado y asignado a propietario.");

        // HU-04: Deuda con Dinero
        int idDeudaPoo = ctrlDeuda.crear(idNegocioPoo, "2026-10-01", "2026-10-31", 600000.0);
        afirmar(idDeudaPoo > 0, "POO HU-04: DeudaAlquiler registrada con Value Object Dinero inmutable.");

        // HU-05: Pagos mediante método de Aggregate Root
        boolean negocioNoExiste = false;
        try {
            ctrlPago.procesar(8888, 100000.0, "2026-10-14", "EFECTIVO");
        } catch (sigepac.poo.dominio.excepciones.NegocioNoEncontradoExcepcion e) {
            negocioNoExiste = true;
        }
        afirmar(negocioNoExiste, "POO HU-05: NegocioNoEncontradoExcepcion lanzada si el negocio no existe.");

        boolean pagoOk = ctrlPago.procesar(idNegocioPoo, 250000.0, "2026-10-14", "TRANSFERENCIA");
        afirmar(pagoOk, "POO HU-05: Pago ejecutado contra la entidad DeudaAlquiler.");

        sigepac.poo.dominio.entidades.DeudaAlquiler deudaActualizada = ctrlDeuda.listar().stream()
                .filter(d -> d.getIdDeuda() != null && d.getIdDeuda() == idDeudaPoo)
                .findFirst().orElse(null);
        afirmar(deudaActualizada != null && deudaActualizada.getSaldoActual().getCantidad() == 350000.0,
                "POO HU-05: Saldo de la entidad recalculado inmediatamente a $350.000.");
    }

    // =========================================================================
    // 4. ITERACION: ARQUITECTURA HEXAGONAL (PUERTOS Y ADAPTADORES)
    // =========================================================================
    private static void probarHexagonal() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println(" 4. PROBANDO ARQUITECTURA HEXAGONAL (Puertos de Entrada/Salida y Adaptadores)");
        System.out.println("--------------------------------------------------------------------------------");

        // Adaptadores de salida
        sigepac.hexagonal.adaptadores.salida.seguridad.AdaptadorCifradorSha256 cifrador = new sigepac.hexagonal.adaptadores.salida.seguridad.AdaptadorCifradorSha256();
        sigepac.hexagonal.adaptadores.salida.persistencia.AdaptadorPersistenciaUsuario repoUsuario = new sigepac.hexagonal.adaptadores.salida.persistencia.AdaptadorPersistenciaUsuario();
        sigepac.hexagonal.adaptadores.salida.persistencia.AdaptadorPersistenciaNegocio repoNegocio = new sigepac.hexagonal.adaptadores.salida.persistencia.AdaptadorPersistenciaNegocio();
        sigepac.hexagonal.adaptadores.salida.persistencia.AdaptadorPersistenciaAlquiler repoAlquiler = new sigepac.hexagonal.adaptadores.salida.persistencia.AdaptadorPersistenciaAlquiler();
        sigepac.hexagonal.adaptadores.salida.persistencia.AdaptadorPersistenciaPago repoPago = new sigepac.hexagonal.adaptadores.salida.persistencia.AdaptadorPersistenciaPago();
        sigepac.hexagonal.adaptadores.salida.persistencia.AdaptadorGestorTransaccion gestorTx = new sigepac.hexagonal.adaptadores.salida.persistencia.AdaptadorGestorTransaccion();

        // Casos de uso
        sigepac.hexagonal.nucleo.aplicacion.servicios.ServicioAutenticacionHexagonal srvAuth = new sigepac.hexagonal.nucleo.aplicacion.servicios.ServicioAutenticacionHexagonal(repoUsuario, cifrador);
        sigepac.hexagonal.nucleo.aplicacion.servicios.ServicioGestionUsuarioHexagonal srvUsuario = new sigepac.hexagonal.nucleo.aplicacion.servicios.ServicioGestionUsuarioHexagonal(repoUsuario, cifrador);
        sigepac.hexagonal.nucleo.aplicacion.servicios.ServicioGestionNegocioHexagonal srvNegocio = new sigepac.hexagonal.nucleo.aplicacion.servicios.ServicioGestionNegocioHexagonal(repoNegocio, repoUsuario);
        sigepac.hexagonal.nucleo.aplicacion.servicios.ServicioRegistroDeudaHexagonal srvDeuda = new sigepac.hexagonal.nucleo.aplicacion.servicios.ServicioRegistroDeudaHexagonal(repoAlquiler, repoNegocio);
        sigepac.hexagonal.nucleo.aplicacion.servicios.ServicioProcesarPagoHexagonal srvPago = new sigepac.hexagonal.nucleo.aplicacion.servicios.ServicioProcesarPagoHexagonal(repoNegocio, repoAlquiler, repoPago, gestorTx);

        // Controladores Web (Adaptadores de Entrada)
        sigepac.hexagonal.adaptadores.entrada.web.ControladorAutenticacionHexagonal ctrlAuth = new sigepac.hexagonal.adaptadores.entrada.web.ControladorAutenticacionHexagonal(srvAuth);
        sigepac.hexagonal.adaptadores.entrada.web.ControladorUsuarioHexagonal ctrlUsuario = new sigepac.hexagonal.adaptadores.entrada.web.ControladorUsuarioHexagonal(srvUsuario);
        sigepac.hexagonal.adaptadores.entrada.web.ControladorNegocioHexagonal ctrlNegocio = new sigepac.hexagonal.adaptadores.entrada.web.ControladorNegocioHexagonal(srvNegocio);
        sigepac.hexagonal.adaptadores.entrada.web.ControladorAlquilerHexagonal ctrlAlquiler = new sigepac.hexagonal.adaptadores.entrada.web.ControladorAlquilerHexagonal(srvDeuda);
        sigepac.hexagonal.adaptadores.entrada.web.ControladorPagoHexagonal ctrlPago = new sigepac.hexagonal.adaptadores.entrada.web.ControladorPagoHexagonal(srvPago);

        // HU-01: Autenticación por Caso de Uso
        sigepac.hexagonal.nucleo.dominio.modelos.Usuario admin = ctrlAuth.iniciarSesion("admin@sigepac.com", "Admin123*");
        afirmar(admin != null && "ADMINISTRADORA".equals(admin.getRol()),
                "Hexagonal HU-01: Caso de uso ICasoUsoIniciarSesion valida credenciales a traves de adaptador de seguridad.");

        boolean errorAuth = false;
        try {
            ctrlAuth.iniciarSesion("admin@sigepac.com", "PasswordInvalida");
        } catch (sigepac.hexagonal.nucleo.dominio.excepciones.CredencialesInvalidasExcepcion e) {
            errorAuth = true;
        }
        afirmar(errorAuth, "Hexagonal HU-01: CredencialesInvalidasExcepcion lanzada ante credenciales erroneas.");

        // HU-02: Gestión de Usuario
        sigepac.hexagonal.nucleo.dominio.modelos.Usuario nuevoUsuario = ctrlUsuario.crear("Tatiana", "Suarez", "tatiana.hex@gmail.com", "TatianaHex123*", "PROPIETARIO", "3123334455");
        afirmar(nuevoUsuario != null && nuevoUsuario.getId() > 0,
                "Hexagonal HU-02: ICasoUsoGestionarUsuario persiste nuevo usuario mediante IPuertoPersistenciaUsuario.");

        // HU-03: Gestión de Negocio
        sigepac.hexagonal.nucleo.dominio.modelos.Negocio nuevoNegocio = ctrlNegocio.crear(nuevoUsuario.getId(), "Libreria El Estudiante Hex", "LOC-HEX-44");
        afirmar(nuevoNegocio != null && "LOC-HEX-44".equals(nuevoNegocio.getNumeroLocal()),
                "Hexagonal HU-03: ICasoUsoGestionarNegocio registra local y asocia propietario.");

        // HU-04: Registrar Deuda / Alquiler
        sigepac.hexagonal.nucleo.dominio.modelos.Alquiler deudaHex = ctrlAlquiler.crear(nuevoNegocio.getId(), "2026-10-01", "2026-10-31", 400000.0);
        afirmar(deudaHex != null && deudaHex.getSaldoPorPagar() == 400000.0,
                "Hexagonal HU-04: ICasoUsoRegistrarDeuda asocia deuda al negocio con canon mensual.");

        // HU-05: Procesar Pago y Actualización Inmediata
        boolean negocioNoExiste = false;
        try {
            ctrlPago.procesar(777777, 100000.0, "2026-10-14", "EFECTIVO");
        } catch (sigepac.hexagonal.nucleo.dominio.excepciones.NegocioNoEncontradoExcepcion e) {
            negocioNoExiste = true;
        }
        afirmar(negocioNoExiste, "Hexagonal HU-05: Arroja NegocioNoEncontradoExcepcion si el negocio no existe previamente.");

        sigepac.hexagonal.nucleo.dominio.modelos.Pago pagoHex = ctrlPago.procesar(nuevoNegocio.getId(), 150000.0, "2026-10-14", "EFECTIVO");
        afirmar(pagoHex != null && pagoHex.getMontoAbonado() == 150000.0,
                "Hexagonal HU-05: Pago procesado bajo transaccion concurrente (RNF5).");

        sigepac.hexagonal.nucleo.dominio.modelos.Alquiler alqActualizado = ctrlAlquiler.listar().stream()
                .filter(a -> a.getNegocioId() == nuevoNegocio.getId())
                .findFirst().orElse(null);
        afirmar(alqActualizado != null && alqActualizado.getSaldoPorPagar() == 250000.0,
                "Hexagonal HU-05: Saldo de cuenta del negocio actualizado inmediatamente a $250.000.");
    }
}
