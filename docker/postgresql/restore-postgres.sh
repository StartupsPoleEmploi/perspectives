#!/bin/bash

cat dump_05-06-2018_17_49_50.sql | sudo docker exec -i postgresql-backup psql -U perspectives