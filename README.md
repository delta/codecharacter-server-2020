# Codecharacter Server 2021

Spring Boot server for Codecharacter 2021

## Requirements

### Mandatory

1. [JDK-11](https://linuxize.com/post/install-java-on-ubuntu-18-04/)
2. [MongoDB](https://docs.mongodb.com/manual/tutorial/install-mongodb-on-ubuntu/)
3. [Rabbitmq](https://www.digitalocean.com/community/tutorials/how-to-install-and-manage-rabbitmq)
   
### Optional
1. Docker
2. IDE - Intellij IDEA (Preferably, makes life easier) (Preferably Ultimate edition as it has premier SpringBoot support)
3. MongoDB GUI - Robo 3T (Optional)

## Local Setup

1. Clone the repository
2. Make a file named application.properties in `src/main/resources` folder and copy the contents of application.properties.example
3. Run `./storage.sh`
3. Set the mongodb port and also update the compilebox secret key in application.properties. Also fill the rabbitmq queue name. You can get the queue name from compilebox config.
4. Make sure rabbitmq and mongodb are running (You can check with command `sudo systemctl status rabbitmq`)
5. Run `mongo localhost:27017/codecharacter-2021 config/mongodb/mongo-init.js`
6. Make sure nothing else is running in port 8080 (especially Tomcat)
7. Run `./gradlew bootRun` to start the server. The server is hosted in `/server`
8. If it was successful your server will be hosted at `localhost:8080/server`. Port and context path can be customised in application.properties
9. The Swagger-UI will be hosted at `localhost:8080/server/swagger-ui.html`. Note: Swagger-UI will be displayed only if you're logged in.
10. While testing from frontend, if you encounter an error due to CORS, run Chrome with `chrome-stable --disable-web-security --user-data-dir=/tmp/chrome`.

## Docker Setup

1. Clone the repository and alter the contents of src/main/resources/application.properties.docker as required (defaults should work fine)
2. Run `storage.sh` to initialise the storage dir
3. Run `docker-compose build` and then `docker-compose up`
4. Swagger is hosted at `localhost:8086/server/swagger-ui/index.html`
5. Run `docker-compose down` to remove the docker containers
6. Use the port number in the compose file to connect to db using MongoDB GUI
