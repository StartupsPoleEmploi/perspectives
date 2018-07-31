#!/bin/bash

CONTAINER_ID=`sudo docker run -d --name postgresql-backup --mount src=perspectives_postgresql,target=/var/lib/postgresql/data postgres:10.4`

echo "CONTAINER : $CONTAINER_ID"

sudo docker exec -it postgresql-backup pg_dump -U perspectives -t candidats_cv > dump-candidats-cv-`date +%d-%m-%Y"_"%H_%M_%S`.sql

sudo docker container stop postgresql-backup

sudo docker container rm $CONTAINER_ID
