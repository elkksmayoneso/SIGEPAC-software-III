-- ============================================================================
-- SIGEPAC: Sistema de Gestión de Pagos de Administración para Plaza Cívica
-- ITERACIÓN 4: ARQUITECTURA HEXAGONAL (PUERTOS Y ADAPTADORES) EN JAVA
-- Motor: MySQL 8.x / MariaDB (InnoDB con integridad referencial)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS sigepac_hexagonal_java CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sigepac_hexagonal_java;

-- 1. Tabla de Usuarios (Hexagonal Core)
CREATE TABLE IF NOT EXISTS hex_usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    hash_password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL DEFAULT 'PROPIETARIO',
    telefono VARCHAR(30) NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_correo (correo)
) ENGINE=InnoDB;

-- 2. Tabla de Negocios
CREATE TABLE IF NOT EXISTS hex_negocios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    propietario_id INT NOT NULL,
    nombre_comercial VARCHAR(150) NOT NULL,
    numero_local VARCHAR(50) NOT NULL UNIQUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hex_neg_prop FOREIGN KEY (propietario_id) 
        REFERENCES hex_usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_neg_prop (propietario_id)
) ENGINE=InnoDB;

-- 3. Tabla de Alquileres
CREATE TABLE IF NOT EXISTS hex_alquileres (
    id INT AUTO_INCREMENT PRIMARY KEY,
    negocio_id INT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_corte DATE NOT NULL,
    canon_mensual DECIMAL(12,2) NOT NULL CHECK (canon_mensual >= 0),
    saldo_por_pagar DECIMAL(12,2) NOT NULL CHECK (saldo_por_pagar >= 0),
    estado_deuda VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hex_alq_neg FOREIGN KEY (negocio_id) 
        REFERENCES hex_negocios(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_alq_neg (negocio_id)
) ENGINE=InnoDB;

-- 4. Tabla de Pagos
CREATE TABLE IF NOT EXISTS hex_pagos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    alquiler_id INT NOT NULL,
    monto_abonado DECIMAL(12,2) NOT NULL CHECK (monto_abonado > 0),
    fecha_abono DATETIME NOT NULL,
    forma_abono VARCHAR(50) NOT NULL DEFAULT 'EFECTIVO',
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hex_pag_alq FOREIGN KEY (alquiler_id) 
        REFERENCES hex_alquileres(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_pag_alq (alquiler_id)
) ENGINE=InnoDB;

-- Administradora Inicial
INSERT INTO hex_usuarios (nombre, apellido, correo, hash_password, rol, telefono)
VALUES ('Admin', 'Hexagonal', 'admin@sigepac.com', 
        'e8rOsu9rPqPZZcK1f86h3uT0k7H7aRkWlXN29Bv0qL8k4h89eQOqO', 
        'ADMINISTRADORA', '3200001122')
ON DUPLICATE KEY UPDATE correo=correo;
