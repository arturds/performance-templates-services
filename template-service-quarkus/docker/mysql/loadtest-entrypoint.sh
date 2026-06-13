#!/bin/bash
set -euo pipefail

MYSQL_HOST="127.0.0.1"
MYSQL_USER="root"
MYSQL_PASS="${MYSQL_ROOT_PASSWORD}"
MYSQL_CMD=(mysql -h"${MYSQL_HOST}" -u"${MYSQL_USER}" -p"${MYSQL_PASS}" --protocol=TCP)

rm -f /tmp/mysql-loadtest-ready

/usr/local/bin/docker-entrypoint.sh "$@" &
entrypoint_pid=$!

echo "Waiting for MySQL to accept TCP connections on ${MYSQL_HOST}..."
for _ in $(seq 1 120); do
  if mysqladmin ping -h"${MYSQL_HOST}" -u"${MYSQL_USER}" -p"${MYSQL_PASS}" --protocol=TCP --silent 2>/dev/null; then
    break
  fi
  if ! kill -0 "${entrypoint_pid}" 2>/dev/null; then
    echo "ERROR: mysqld exited before becoming ready."
    wait "${entrypoint_pid}" || true
    exit 1
  fi
  sleep 0.5
done

if ! mysqladmin ping -h"${MYSQL_HOST}" -u"${MYSQL_USER}" -p"${MYSQL_PASS}" --protocol=TCP --silent 2>/dev/null; then
  echo "ERROR: MySQL did not become ready within 60 seconds."
  exit 1
fi

echo "Disabling InnoDB redo log (load test only)..."
if "${MYSQL_CMD[@]}" -e "ALTER INSTANCE DISABLE INNODB REDO_LOG;"; then
  echo "InnoDB redo log disabled successfully."
else
  echo "WARNING: Could not disable InnoDB redo log (see message above)."
fi

if ! mysqladmin ping -h"${MYSQL_HOST}" -u"${MYSQL_USER}" -p"${MYSQL_PASS}" --protocol=TCP --silent 2>/dev/null; then
  echo "ERROR: MySQL stopped responding after redo log change."
  exit 1
fi

touch /tmp/mysql-loadtest-ready
echo "MySQL load-test bootstrap complete."

wait "${entrypoint_pid}"
