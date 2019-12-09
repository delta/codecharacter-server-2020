# Codecharacter Server 2020

Spring Boot server for Codecharacter 2020

## Requirements

1. JDK 11 or later
2. Tomcat 9
3. MongoDB
4. IDE - Intellij IDEA (Preferably, makes life easier)
5. MongoDB GUI - Robo 3T (Optional)

## Setup

1. Clone the repository
2. Set `tomcat.dir`(Path to root of tomcat installed location) and `app.name` in build.properties
3. Set MongoDB properties in `src/main/resources/application.properties`
4. Run `ant run` to compile and start the tomcat server
5. The server is hosted at `localhost:{tomcat_port_no}/{app.name}`. Tomcat port is by default set to 8080.
6. Swagger is hosted at `localhost:{tomcat_port_no}/{app.name}/swagger-ui.html`
