-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建开发环境数据库，明确指定字符集
CREATE DATABASE IF NOT EXISTS codivio_user_dev 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS codivio_project_dev 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS codivio_system_dev 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权
CREATE USER IF NOT EXISTS 'codivio_dev'@'%' IDENTIFIED BY 'codivio123';
GRANT ALL PRIVILEGES ON codivio_user_dev.* TO 'codivio_dev'@'%';
GRANT ALL PRIVILEGES ON codivio_project_dev.* TO 'codivio_dev'@'%';
GRANT ALL PRIVILEGES ON codivio_system_dev.* TO 'codivio_dev'@'%';

FLUSH PRIVILEGES;

SET FOREIGN_KEY_CHECKS = 1;
