#!/bin/bash
export PGDATA=/var/lib/postgresql/data
export KAFKA_HOME=/opt/kafka
export PG_BIN_DIR=$(ls -d /usr/lib/postgresql/*/bin | head -n 1)

set -e
if [ -z "$(ls -A "$PGDATA" 2>/dev/null)" ]; then
    mkdir -p "$PGDATA" && chown -R postgres:postgres "$PGDATA"
    gosu postgres "$PG_BIN_DIR/initdb" -D "$PGDATA"
    gosu postgres "$PG_BIN_DIR/pg_ctl" -D "$PGDATA" -w start
    gosu postgres psql -c "CREATE USER dtrack WITH PASSWORD 'dtrack';"
    gosu postgres psql -c "CREATE DATABASE dtrack OWNER dtrack;"
    gosu postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE dtrack TO dtrack;"
    gosu postgres "$PG_BIN_DIR/pg_ctl" -D "$PGDATA" -m fast -w stop
fi
mkdir -p /run/postgresql && chown -R postgres:postgres /run/postgresql
exit 0

