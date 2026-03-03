#!/usr/bin/env bash

export IMAGE=$1
cd /home/ec2-user
docker-compose -f docker-compose.yaml down
docker-compose -f docker-compose.yaml up --detach
echo "success"