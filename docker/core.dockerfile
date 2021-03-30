FROM openjdk:11
WORKDIR /app
RUN apt-get update -y && apt-get upgrade -y \
&& apt-get install -y ant wget maven unzip \
&& wget https://apachemirror.wuchna.com/tomcat/tomcat-9/v9.0.43/bin/apache-tomcat-9.0.43.zip -O /tomcat.zip \
&& unzip /tomcat.zip -d / \
&& mv /apache-tomcat-9.0.43 /tomcat && chmod -R +x /tomcat \
&& rm -rf /tomcat/webapps/ROOT \
&& rm -rf /tomcat.zip \
&& java --version
