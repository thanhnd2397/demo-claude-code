-- Creates the replication user consumed by mysql-replica's CHANGE REPLICATION SOURCE command.
CREATE USER IF NOT EXISTS 'repl'@'%' IDENTIFIED BY 'repl_password';
GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%';
FLUSH PRIVILEGES;
