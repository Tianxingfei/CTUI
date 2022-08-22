#################################################################
## Dockerfile for creating docker image: Spark on Ubuntu 18.04 ##
#################################################################

#select ubuntu version
FROM ubuntu:18.04

#label to recognise this image
LABEL image=mdf-spark-image

#set spark version
ENV SPARK_VERSION=2.4.4
ENV HADOOP_VERSION=2.7

#install the below packages on the ubuntu image
RUN apt-get update -qq && \
    apt-get install -qq -y gnupg2 wget openjdk-8-jdk scala

#set working directory
WORKDIR /

#download the Spark binaries
RUN wget https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz

#untar the downloaded binaries, move them the folder name spark and add the spark bin on my class path
RUN tar xvf spark-* && \
    mv spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION} /opt/spark && \
    echo "export PATH=$PATH:/opt/spark/bin:/opt/spark/sbin" >> ~/.bashrc && \
    echo "export SPARK_HOME=/opt/spark"  >>  ~/.bashrc

#persist environment variables (exporting alone doesn't work)
ENV SPARK_HOME=/opt/spark
ENV PATH=$PATH:/opt/spark/bin:/opt/spark/sbin

#expose port for spark web UI
EXPOSE 4040

#install locales for several languages support
RUN apt-get install -qq -y locales && \
    locale-gen en_US.UTF-8
ENV LANG=en_US.UTF-8

#MongoDB installation
RUN apt-get install -qq -y apt-transport-https ca-certificates && \
    wget -qO - https://www.mongodb.org/static/pgp/server-4.0.asc | apt-key add - && \
    echo "deb https://repo.mongodb.org/apt/ubuntu xenial/mongodb-org/4.0 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-4.0.list && \
    apt-get update -qq && \
    mkdir -p /data/db && \
    apt-get install -qq -y mongodb-org

#configure Mongo to run on given port and access from all IPs
ENTRYPOINT mongod --bind_ip_all --port 49249 && bash


##############################
## instructions to run BDD  ##
##############################

#open runBddOnDocker.bat and set the values on line 3-6
#open file explorer and double click runBddOnDocker.bat file
