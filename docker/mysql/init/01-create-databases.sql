-- 创建数据库
CREATE DATABASE IF NOT EXISTS codivio_user CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS codivio_project CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS codivio_file CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权
CREATE USER IF NOT EXISTS 'codivio'@'%' IDENTIFIED BY 'codivio123';
GRANT ALL PRIVILEGES ON codivio_user.* TO 'codivio'@'%';
GRANT ALL PRIVILEGES ON codivio_project.* TO 'codivio'@'%';
GRANT ALL PRIVILEGES ON codivio_file.* TO 'codivio'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 使用用户数据库
USE codivio_user;