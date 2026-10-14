-- ============================================================================
-- SIGEPAC: Sistema de Gestión de Pagos de Administración para Plaza Cívica
-- ITERACIÓN 2: ARQUITECTURA EN CAPAS EN JAVA - ESQUEMA DE BASE DE DATOS AISLADA
-- Motor: MySQL 8.x / MariaDB (InnoDB con integridad referencial)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS sigepac_capas_java CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sigepac_capas_java;

-- 1. Tabla de Usuarios (Capa de Acceso a Datos)
CREATE TABLE IF NOT EXISTS tbl_usuarios (
    usr_id INT AUTO_INCREMENT PRIMARY KEY,
    usr_nombre VARCHAR(80) NOT NULL,
    usr_apellido VARCHAR(80) NOT NULL,
    usr_correo VARCHAR(120) NOT NULL UNIQUE,
    usr_contrasena_hash VARCHAR(255) NOT NULL,
    usr_rol VARCHAR(30) NOT NULL DEFAULT 'PROPIETARIO',
    usr_telefono VARCHAR(25) NOT NULL,
    usr_creado DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_usr_correo (usr_correo)
) ENGINE=InnoDB;

-- 2. Tabla de Negocios
CREATE TABLE IF NOT EXISTS tbl_negocios (
    neg_id INT AUTO_INCREMENT PRIMARY KEY,
    usr_id INT NOT NULL,
    neg_nombre VARCHAR(120) NOT NULL,
    neg_local_codigo VARCHAR(30) NOT NULL UNIQUE,
    neg_activo TINYINT(1) NOT NULL DEFAULT 1,
    neg_fecha_alta DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_capas_neg_usr FOREIGN KEY (usr_id) 
        REFERENCES tbl_usuarios(usr_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_neg_usr (usr_id)
) ENGINE=InnoDB;

-- 3. Tabla de Alquileres / Deudas
CREATE TABLE IF NOT EXISTS tbl_alquileres (
    alq_id INT AUTO_INCREMENT PRIMARY KEY,
    neg_id INT NOT NULL,
    alq_fecha_inicio DATE NOT NULL,
    alq_fecha_vencimiento DATE NOT NULL,
    alq_valor_cuota DECIMAL(12,2) NOT NULL CHECK (alq_valor_cuota >= 0),
    alq_saldo_actual DECIMAL(12,2) NOT NULL CHECK (alq_saldo_actual >= 0),
    alq_estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_capas_alq_neg FOREIGN KEY (neg_id) 
        REFERENCES tbl_negocios(neg_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_alq_neg (neg_id)
) ENGINE=InnoDB;

-- 4. Tabla de Pagos
CREATE TABLE IF NOT EXISTS tbl_pagos (
    pag_id INT AUTO_INCREMENT PRIMARY KEY,
    alq_id INT NOT NULL,
    pag_monto DECIMAL(12,2) NOT NULL CHECK (pag_monto > 0),
    pag_fecha_registro DATETIME NOT NULL,
    pag_forma_pago VARCHAR(50) NOT NULL DEFAULT 'EFECTIVO',
    CONSTRAINT fk_capas_pag_alq FOREIGN KEY (alq_id) 
        REFERENCES tbl_alquileres(alq_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_pag_alq (alq_id)
) ENGINE=InnoDB;

-- Administradora Inicial
INSERT INTO tbl_usuarios (usr_nombre, usr_apellido, usr_correo, usr_contrasena_hash, usr_rol, usr_telefono)
VALUES ('Admin', 'Capas', 'admin@sigepac.com', 
        'e8rOsu9rPqPZZcK1f86h3uT0k7H7aRkWlXN29Bv0qL8k4h89eQOqO', 
        'ADMINISTRADORA', '3009998877')
ON DUPLICATE KEY UPDATE usr_correo=usr_correo;
