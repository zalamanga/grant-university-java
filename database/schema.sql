CREATE TABLE accounts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(40) NOT NULL UNIQUE,
  name VARCHAR(161) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE profiles (
  account_id BIGINT PRIMARY KEY,
  first_name VARCHAR(80) NOT NULL,
  last_name VARCHAR(80) NOT NULL,
  birth_date DATE NOT NULL,
  gender VARCHAR(30) NOT NULL,
  address VARCHAR(400) NOT NULL,
  phone VARCHAR(30) NOT NULL,
  FOREIGN KEY(account_id) REFERENCES accounts(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE courses (
  id VARCHAR(10) PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  duration VARCHAR(80) NOT NULL,
  eligibility VARCHAR(150) NOT NULL,
  fee VARCHAR(150) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Nilai contoh dipertahankan dari CourseDetailsBean lama, bukan informasi program aktual.
INSERT INTO courses(id,name,duration,eligibility,fee) VALUES
('A-101','Science','4 tahun','70% di high school (contoh sumber)','USD 5000 per tahun (contoh sumber)'),
('A-105','Law','5 tahun','60% di high school (contoh sumber)','USD 3000 per tahun (contoh sumber)'),
('A-110','Medical','6 tahun','70% di high school (contoh sumber)','USD 7000 per tahun (contoh sumber)'),
('A-115','Management','2 tahun','50% di high school (contoh sumber)','USD 5000 per tahun (contoh sumber)');
