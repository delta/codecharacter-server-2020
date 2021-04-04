
docker-compose \
  -f docker/docker-compose.yml \
  --project-name codechar \
  --project-directory . \
  up --build -d
