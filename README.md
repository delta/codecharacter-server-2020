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

1. IDE - Intellij IDEA (Preferably, makes life easier)
2. MongoDB GUI - Robo 3T 

## Setup

1. Clone the repository
2. Create a file named build.properties and copy the contents of build.properties.example
3. Update the tomcat.dir field in build.properties file. (it's value should be the path to root of Tomcat installed location)
4. Make a file named application.properties in `src/main/resources` folder and copy the contents of application.properties.example
5. Set the mongodb port and also update the compilebox secret key in application.properties
6. Make sure the rabbitmq-server is running (You can check with command `sudo systemctl status rabbitmq-server`)
7. Check the status of tomcat server with `sudo systemctl status tomcat`. If it's active, stop it manually with the command `sudo systemctl stop tomcat`.
8. Run `ant run` to compile and start the tomcat server.
9. If it was successful your server will be hosted at `localhost:{tomcat_port_no}/{app.name}`. Tomcat port is by default 8080 and app.name is present at build.properties file



