#!/bin/bash

source /opt/postgresql-cron-backup/pgenv.sh

pg_dump -T candidats_cv > $BACKUP_DIR/dump-sans-candidats-cv-`date +%Y-%m-%d"_"%H_%M_%S`.sql

pg_dump -t candidats_cv > $BACKUP_DIR/dump-candidats-cv-`date +%Y-%m-%d"_"%H_%M_%S`.sql