-- Run as a MySQL administrator. Replace this example password first.
CREATE DATABASE IF NOT EXISTS foodexpress CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'foodexpress_app'@'%' IDENTIFIED BY 'CHANGE_THIS_PASSWORD';
GRANT ALL PRIVILEGES ON foodexpress.* TO 'foodexpress_app'@'%';
-- No global privileges granted. Hibernate creates the application tables.
