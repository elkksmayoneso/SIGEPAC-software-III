-- ============================================================================
-- SIGEPAC: Sistema de Gestión de Pagos de Administración para Plaza Cívica
-- ITERACIÓN 1: ARQUITECTURA MVC EN JAVA - ESQUEMA DE BASE DE DATOS AISLADA
-- Motor: MySQL 8.x / MariaDB (InnoDB con integridad referencial)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS sigepac_mvc_java CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sigepac_mvc_java;

-- 1. Tabla de Usuarios (HU-01, HU-02)
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL DEFAULT 'PROPIETARIO',
    telefono VARCHAR(20) NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_correo (correo)
) ENGINE=InnoDB;

-- 2. Tabla de Negocios (HU-03)
CREATE TABLE IF NOT EXISTS negocios (
    id_negocio INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    local_numero VARCHAR(50) NOT NULL UNIQUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mvc_neg_usr FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_neg_usr (id_usuario)
) ENGINE=InnoDB;

-- 3. Tabla de Alquileres / Deudas (HU-04)
CREATE TABLE IF NOT EXISTS alquileres (
    id_alquiler INT AUTO_INCREMENT PRIMARY KEY,
    id_negocio INT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_limite DATE NOT NULL,
    valor_total DECIMAL(12, 2) NOT NULL CHECK (valor_total >= 0),
    saldo_pendiente DECIMAL(12, 2) NOT NULL CHECK (saldo_pendiente >= 0),
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mvc_alq_neg FOREIGN KEY (id_negocio)
        REFERENCES negocios(id_negocio) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_alq_neg (id_negocio)
) ENGINE=InnoDB;

-- 4. Tabla de Pagos (HU-05)
CREATE TABLE IF NOT EXISTS pagos (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_alquiler INT NOT NULL,
    monto DECIMAL(12, 2) NOT NULL CHECK (monto > 0),
    fecha_pago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metodo_pago VARCHAR(50) NOT NULL DEFAULT 'EFECTIVO',
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mvc_pag_alq FOREIGN KEY (id_alquiler)
        REFERENCES alquileres(id_alquiler) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_pag_alq (id_alquiler)
) ENGINE=InnoDB;

-- Usuario Inicial por defecto (Administradora: admin@sigepac.com / Contraseña: Admin123*)
INSERT INTO usuarios (nombre, apellido, correo, contrasena_hash, rol, telefono)
VALUES ('Administradora', 'Plaza Cívica', 'admin@sigepac.com',
        '42e5ddb2985fd21cff0c867a6fed0e77a51a61024222530729a7ad0ebc39044f',
        'ADMINISTRADORA', '3101234567')
ON DUPLICATE KEY UPDATE contrasena_hash=VALUES(contrasena_hash);
