-- MariaDB 12 compatibility fix for Cloud API Demo schema.
-- Fixes Illegal mix of collations errors seen in firmware queries.
-- Safe to run multiple times.

USE cloud_sample;

ALTER TABLE manage_device_firmware
  MODIFY firmware_id VARCHAR(45)
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_uca1400_ai_ci
  NOT NULL;

ALTER TABLE manage_device_firmware
  MODIFY workspace_id VARCHAR(64)
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_uca1400_ai_ci
  NOT NULL;

ALTER TABLE manage_firmware_model
  MODIFY firmware_id VARCHAR(64)
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_uca1400_ai_ci
  NOT NULL;
