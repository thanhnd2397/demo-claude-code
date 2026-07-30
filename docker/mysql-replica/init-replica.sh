#!/bin/bash
# Points this instance at mysql-primary and starts GTID-based replication.
# Runs once, at first container initialization, via docker-entrypoint-initdb.d.
set -e

echo "Waiting for mysql-primary to accept connections..."
until mysqladmin ping -h mysql-primary -u root -p"${MYSQL_ROOT_PASSWORD}" --silent; do
  sleep 2
done

mysql -u root -p"${MYSQL_ROOT_PASSWORD}" <<-EOSQL
  CHANGE REPLICATION SOURCE TO
    SOURCE_HOST='mysql-primary',
    SOURCE_USER='repl',
    SOURCE_PASSWORD='repl_password',
    SOURCE_AUTO_POSITION=1,
    GET_SOURCE_PUBLIC_KEY=1;
  START REPLICA;
EOSQL

echo "Replication configured."
