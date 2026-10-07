-- Seata 2.5 AT undo schema. Additive and safe to rerun on existing databases.
-- Based on https://github.com/apache/incubator-seata/blob/v2.5.0/script/client/at/db/mysql.sql
USE multi_reward;
CREATE TABLE IF NOT EXISTS undo_log (
    branch_id BIGINT NOT NULL,
    xid VARCHAR(128) NOT NULL,
    context VARCHAR(128) NOT NULL,
    rollback_info LONGBLOB NOT NULL,
    log_status INT NOT NULL,
    log_created DATETIME(6) NOT NULL,
    log_modified DATETIME(6) NOT NULL,
    UNIQUE KEY ux_undo_log (xid, branch_id),
    KEY ix_log_created (log_created)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

USE prize_center;
CREATE TABLE IF NOT EXISTS undo_log (
    branch_id BIGINT NOT NULL,
    xid VARCHAR(128) NOT NULL,
    context VARCHAR(128) NOT NULL,
    rollback_info LONGBLOB NOT NULL,
    log_status INT NOT NULL,
    log_created DATETIME(6) NOT NULL,
    log_modified DATETIME(6) NOT NULL,
    UNIQUE KEY ux_undo_log (xid, branch_id),
    KEY ix_log_created (log_created)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
