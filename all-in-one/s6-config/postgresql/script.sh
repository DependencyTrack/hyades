#!/bin/bash
export PGDATA=/var/lib/postgresql/data
export PG_BIN_DIR=$(ls -d /usr/lib/postgresql/*/bin | head -n 1)

echo "[postgresql] Starting PostgreSQL server..."
exec gosu postgres "$PG_BIN_DIR/postgres" -D "$PGDATA"

