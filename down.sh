#!/bin/sh
PROJECT_FOLDER=`git rev-parse --show-toplevel`

set -x
cd $PROJECT_FOLDER
docker-compose \
    -f docker/docker-compose.yml \
    --project-name codechar \
    --project-directory . \
    down
