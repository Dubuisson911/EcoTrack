-- Asegúrate de estar en la BD correcta
USE EcoTrack;
GO
-- (Opcional) Crea el usuario en la BD si por alguna razón no existe todavía
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'ecotrack_user')
    CREATE USER ecotrack_user FOR LOGIN ecotrack_user;
GO
-- Forma moderna (recomendada) para otorgar permisos de lectura y escritura
ALTER ROLE db_datareader ADD MEMBER ecotrack_user;
ALTER ROLE db_datawriter ADD MEMBER ecotrack_user;
GO

USE EcoTrack;
SELECT rp.name AS [role], mp.name AS [member]
FROM sys.database_role_members drm
JOIN sys.database_principals rp ON rp.principal_id = drm.role_principal_id
JOIN sys.database_principals mp ON mp.principal_id = drm.member_principal_id
WHERE mp.name = 'ecotrack_user';

