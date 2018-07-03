#!/bin/bash

sudo docker run -d --name postgresql-backup --mount src=perspectives_postgresql,target=/var/lib/postgresql/data postgres:10.4

sudo docker exec -it postgresql-backup pg_dumpall -c -U perspectives > dump_`date +%d-%m-%Y"_"%H_%M_%S`.sql

sudo docker container stop postgresql-backup
