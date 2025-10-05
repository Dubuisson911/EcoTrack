------------------------------------------------------------
-- 1️⃣ CREAR BASE DE DATOS
------------------------------------------------------------
CREATE DATABASE EcoTrack;
GO
USE EcoTrack;
GO
------------------------------------------------------------
-- 2️⃣ CREAR LOGIN Y USUARIO APARTE DEL 'sa'
------------------------------------------------------------
-- Crea un nuevo login a nivel del servidor (ajusta contraseña)
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'ecotrack_user')
BEGIN
    CREATE LOGIN ecotrack_user WITH PASSWORD = 'EcoTrack123$', CHECK_POLICY = OFF, CHECK_EXPIRATION = OFF;
END
GO

-- Crear usuario dentro de la base de datos
CREATE USER ecotrack_user FOR LOGIN ecotrack_user;
GO

-- Asignar permisos básicos
EXEC sp_addrolemember 'db_datareader', 'ecotrack_user';
EXEC sp_addrolemember 'db_datawriter', 'ecotrack_user';
GO
------------------------------------------------------------
-- 3️⃣ TABLA DE USUARIOS (LOGIN DE APLICACIÓN)
------------------------------------------------------------
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    role NVARCHAR(20) NOT NULL DEFAULT 'user'
);
GO
------------------------------------------------------------
-- 4️⃣ TABLA DE PUNTOS DE RECICLAJE
------------------------------------------------------------
CREATE TABLE points (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    address NVARCHAR(200) NOT NULL,
    latitude DECIMAL(9,6) NULL,
    longitude DECIMAL(9,6) NULL,
    manager NVARCHAR(100) NULL
);
GO
------------------------------------------------------------
-- 5️⃣ TABLA DE RECOLECCIONES
------------------------------------------------------------
CREATE TABLE collections (
    id INT IDENTITY(1,1) PRIMARY KEY,
    point_id INT NOT NULL,
    date DATE NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('Plástico','Vidrio','Papel')),
    weight_kg DECIMAL(10,2) NOT NULL CHECK (weight_kg > 0),
    FOREIGN KEY (point_id) REFERENCES points(id)
);
GO
------------------------------------------------------------
-- 6️⃣ ÍNDICES PARA RENDIMIENTO
------------------------------------------------------------
CREATE INDEX ix_collections_date ON collections(date);
CREATE INDEX ix_collections_point ON collections(point_id);
GO
------------------------------------------------------------
-- 7️⃣ DATOS DE PRUEBA
------------------------------------------------------------

-- Usuario demo (login dentro de la app, NO SQL Server)
INSERT INTO users (username, password, role) VALUES
('admin', 'admin123', 'admin'),
('operador', 'op123', 'user');

-- Puntos de reciclaje
INSERT INTO points (name, address, latitude, longitude, manager) VALUES
('Punto Central', 'Parque Central, Cobán', 15.4700, -90.3700, 'María López'),
('Colegio UMG', 'Campus UMG Cobán', 15.4780, -90.3650, 'Byron Caal'),
('Barrio Victoria', 'Zona 2, Cobán', 15.4725, -90.3750, 'Lucía Pérez');

-- Recolecciones de prueba
INSERT INTO collections (point_id, date, type, weight_kg) VALUES
(1, '2025-09-25', 'Plástico', 12.5),
(1, '2025-09-27', 'Vidrio', 9.7),
(2, '2025-10-01', 'Papel', 5.4),
(2, '2025-10-03', 'Plástico', 15.2),
(3, '2025-10-04', 'Vidrio', 18.0),
(3, '2025-10-05', 'Papel', 10.6);
GO
------------------------------------------------------------
-- 8️⃣ VERIFICACIÓN RÁPIDA
------------------------------------------------------------
SELECT * FROM users;
SELECT * FROM points;
SELECT * FROM collections;
GO
