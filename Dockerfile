FROM --platform=amd64  openjdk:11-jdk
ADD target/engine-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

#RUN apt-get update && apt-get -y upgrade
#ENV HOME=/app
#RUN mkdir -p $HOME
#WORKDIR $HOME
