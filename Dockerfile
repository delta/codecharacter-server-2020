FROM openjdk:11
RUN apt-get update && apt-get upgrade
RUN apt-get install -y ant wget maven unzip
WORKDIR /app
RUN wget http://apachemirror.wuchna.com/tomcat/tomcat-9/v9.0.31/bin/apache-tomcat-9.0.31.zip -O tomcat.zip
RUN unzip tomcat.zip -d /app
RUN mv apache-tomcat-9.0.31 tomcat && chmod -R +x tomcat
RUN rm -rf /app/tomcat/webapps/ROOT
RUN java --version
COPY . /app
CMD ant run
