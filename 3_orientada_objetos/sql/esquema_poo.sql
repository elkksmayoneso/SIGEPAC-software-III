-- ============================================================================
-- SIGEPAC: Sistema de Gestión de Pagos de Administración para Plaza Cívica
-- ITERACIÓN 3: ARQUITECTURA ORIENTADA A OBJETOS (DOMINIO RICO) EN JAVA
-- Motor: MySQL 8.x / MariaDB (InnoDB con integridad referencial)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS sigepac_poo_java CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sigepac_poo_java;

-- 1. Cuentas de Usuario de Dominio
CREATE TABLE IF NOT EXISTS oop_cuentas_usuario (
    id_cuenta INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_correo (correo)
) ENGINE=InnoDB;

-- 2. Negocios Comerciales
CREATE TABLE IF NOT EXISTS oop_negocios_comerciales (
    id_negocio INT AUTO_INCREMENT PRIMARY KEY,
    id_propietario INT NOT NULL,
    nombre_comercial VARCHAR(150) NOT NULL,
    numero_local VARCHAR(50) NOT NULL UNIQUE,
    esta_activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_poo_biz_prop FOREIGN KEY (id_propietario)
        REFERENCES oop_cuentas_usuario(id_cuenta) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_biz_prop (id_propietario)
) ENGINE=InnoDB;

-- 3. Deudas y Alquileres de Dominio (Aggregate Root)
CREATE TABLE IF NOT EXISTS oop_deudas_alquiler (
    id_deuda INT AUTO_INCREMENT PRIMARY KEY,
    id_negocio INT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_limite DATE NOT NULL,
    monto_original DECIMAL(12,2) NOT NULL CHECK (monto_original >= 0),
    saldo_actual DECIMAL(12,2) NOT NULL CHECK (saldo_actual >= 0),
    estado VARCHAR(20) NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_poo_deuda_biz FOREIGN KEY (id_negocio)
        REFERENCES oop_negocios_comerciales(id_negocio) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_deuda_biz (id_negocio)
) ENGINE=InnoDB;

-- 4. Transacciones de Abono
CREATE TABLE IF NOT EXISTS oop_transacciones_pago (
    id_transaccion INT AUTO_INCREMENT PRIMARY KEY,
    id_deuda INT NOT NULL,
    monto DECIMAL(12,2) NOT NULL CHECK (monto > 0),
    fecha_registro DATETIME NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL DEFAULT 'EFECTIVO',
    CONSTRAINT fk_poo_pago_deuda FOREIGN KEY (id_deuda)
        REFERENCES oop_deudas_alquiler(id_deuda) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_pago_deuda (id_deuda)
) ENGINE=InnoDB;

-- Administradora Inicial
INSERT INTO oop_cuentas_usuario (nombre, apellido, correo, contrasena_hash, rol, telefono)
VALUES ('Administradora', 'Plaza', 'admin@sigepac.com',
        'a2977b682c0a562dbba8616c92c50ac3468055ea2243ca065b39fc70a6e4fa26',
        'ADMINISTRADORA', '3101234567')
ON DUPLICATE KEY UPDATE contrasena_hash=VALUES(contrasena_hash);
