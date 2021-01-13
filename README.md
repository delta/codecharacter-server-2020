# Codecharacter Server 2020

Spring Boot server for Codecharacter 2020

## Requirements

### Mandatory

1. [JDK-11](https://linuxize.com/post/install-java-on-ubuntu-18-04/)
2. [Tomcat-9](https://linuxize.com/post/how-to-install-tomcat-9-on-ubuntu-20-04/)
3. [Ant](https://medium.com/@girishkr/install-apache-ant-1-10-on-ubuntu-16-04-7e249765e1bc)
4. [Maven](https://linuxize.com/post/how-to-install-apache-maven-on-ubuntu-20-04/) 
5. [MongoDB](https://docs.mongodb.com/manual/tutorial/install-mongodb-on-ubuntu/)
6. [Rabbitmq](https://www.digitalocean.com/community/tutorials/how-to-install-and-manage-rabbitmq)
   
### Optional
1. Docker
2. IDE - Intellij IDEA (Preferably, makes life easier)
3. MongoDB GUI - Robo 3T (Optional)

## Local Setup

1. Clone the repository
2. Create a file named build.properties and copy the contents of build.properties.example
3. Update the tomcat.dir field in build.properties file. (it's value should be the path to root of Tomcat installed location)
4. Make a file named application.properties in `src/main/resources` folder and copy the contents of application.properties.example
5. Set the mongodb port and also update the compilebox secret key in application.properties. Also fill the rabbitmq queue name. You can get the queue name   from compilebox config.
6. Make sure the rabbitmq-server is running (You can check with command `sudo systemctl status rabbitmq-server`)
7. Check the status of tomcat server with `sudo systemctl status tomcat`. If it's active, stop it manually with the command `sudo systemctl stop tomcat`.
8. Run `ant run` to compile, build and start the tomcat server, 
   
    or `mvn spring-boot:run` to compile and start. 
9. If it was successful your server will be hosted at `localhost:{tomcat_port_no}/{app.name}`. Tomcat port is by default 8080 and app.name is present at build.properties file
10. The Swagger-UI will be hosted at `localhost:{tomcat_port_no}/{app.name}/swagger-ui.html`. Note: Swagger-UI will be displayed only if you're logged in.

- While testing from frontend, if you encounter an error due to CORS, do [this](https://alfilatov.com/posts/run-chrome-without-cors/)
- If you wish to change the default tomcat port number do [this](https://www.ibm.com/support/pages/how-do-i-change-default-port-apache-tomcat)
- The storage.dir in application.properties property specifies the folder where defferent code files etc are stored. To change it or to set to a relative path, see [this](https://stackoverflow.com/questions/36940458/specifying-relative-path-in-application-properties-in-spring)
-  The volumes directive in docker-compose.yml file mounts source directories or volumes from your computer at target paths inside the container. So here the folder at path `/home/code/Server/storage` in host will be mounted on `/app/tomcat/bin/storage` in the container and that at `/home/code/.m2` will be mounted on `/root/.m2` (see docker-compose.yml file).

## Docker Setup

1. Clone the repository

   Create a file named build.properties and copy the contents of build.properties.example

   Make a file named application.properties in `src/main/resources` folder and copy the contents of application.properties.example
2. run `storage.sh` with sudo once after cloning to initialise the storage dir
2. run `start.sh` to start the server
4. Run `ant run` to inside the docker terminal. Just hit `Ctrl+C` in the terminal and run again after making changes, no need to restart docker
5. The server is hosted at `localhost:{tomcat_port_no}/`. Tomcat port is by default set to 8080 (mapped to 8086 in docker).
6. Swagger is hosted at `localhost:{tomcat_port_no}/{app.name}/swagger-ui.html`
7. run `down.sh` to remove the docker containers
8. use the port number in the compose file to connect to db using MongoDB GUI
