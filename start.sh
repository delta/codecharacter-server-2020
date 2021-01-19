#!/bin/sh
#set -x

if [ -z $1 ]
then
  COMMAND="bash"
else
  COMMAND="${@}"
fi

PROJECT_FOLDER=`git rev-parse --show-toplevel`
export PROJECT_NAME=codechar
cd $PROJECT_FOLDER

docker-compose \
  -f docker/docker-compose.yml \
  -p codechar \
  --project-directory . \
  ps | grep core > /dev/null
#$? is 0 if already running, 1 if not (0=no error)
ALREADY_RUNNING=$?

if [ "$ALREADY_RUNNING" -eq 0 ];
then
  echo "Service already running, only opening shell"
else
  docker-compose \
    -f docker/docker-compose.yml \
    --project-name codechar \
    --project-directory . \
    up -d
fi

echo "Connecting to docker shell and running command $COMMAND..."
docker-compose \
  -f docker/docker-compose.yml \
  --project-name codechar \
  --project-directory . \
  exec core $COMMAND
