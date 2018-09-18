#!/bin/bash

SQL_FILE="$1"

if [ -z $SQL_FILE ] ; then echo "Préciser le fichier SQL à jouer"; exit ; fi

CONTAINER_ID=`sudo docker run -d --name postgresql-execute --mount src=perspectives_postgresql,target=/var/lib/postgresql/data postgres:10.4`

echo "CONTAINER : $CONTAINER_ID"

cat $SQL_FILE | sudo docker exec -it postgresql-execute psql -U perspectives

sudo docker container stop postgresql-execute

sudo docker container rm $CONTAINER_ID