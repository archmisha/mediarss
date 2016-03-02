# Dockerizing MongoDB: Dockerfile for building MongoDB images
# Based on ubuntu:latest, installs MongoDB following the instructions from:
# http://docs.mongodb.org/manual/tutorial/install-mongodb-on-ubuntu/

FROM       ubuntu:14.04
MAINTAINER Docker

# Installation:
# Import MongoDB public GPG key AND create a MongoDB list file
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10 &&
 echo "deb http://repo.mongodb.org/apt/ubuntu "$(lsb_release -sc)"/mongodb-org/3.0 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-3.0.list &&

# Update apt-get sources AND install MongoDB
 apt-get update -o Acquire::http::No-Cache=true &&
 apt-key del 16126D3A3E5C1192 &&
 apt-key adv --recv-keys --keyserver keyserver.ubuntu.com 16126D3A3E5C1192 &&
 apt-get upgrade -y --force-yes -qq &&
 apt-get update -o Acquire::http::No-Cache=true &&

 apt-get install -y mongodb-org &&

# Create the MongoDB data directory
 mkdir -p /data/db

# Expose port #27017 from the container to the host
EXPOSE 27017

VOLUME /data/db

# Set /usr/bin/mongod as the dockerized entry-point application
ENTRYPOINT ["/usr/bin/mongod"]
