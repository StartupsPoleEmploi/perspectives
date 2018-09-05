#!/bin/bash

CONTAINER_ID=`sudo docker run -d --name postgresql-backup --mount src=perspectives_postgresql,target=/var/lib/postgresql/data postgres:10.4`

echo "CONTAINER : $CONTAINER_ID"

sudo docker exec -it postgresql-backup pg_dump -U perspectives -t candidats_cv > dump-candidats-cv-`date +%Y-%m-%d"_"%H_%M_%S`.sql

sudo docker exec -it postgresql-backup pg_dump -U perspectives -T candidats_cv > dump-sans-candidats-cv-`date +%Y-%m-%d"_"%H_%M_%S`.sql

sudo docker container stop postgresql-backup

sudo docker container rm $CONTAINER_ID
