FROM mongo:latest
COPY ./docker/mongo-init.js /docker-entrypoint-initdb.d/
RUN mkdir -p /home/mongodb && touch /home/mongodb/.dbshell \
&& chown -R 999:999 ./home/mongodb