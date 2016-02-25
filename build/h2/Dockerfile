#
# H2 Dockerfile
#

# Pull base image.
FROM stephenreed/jenkins-java8-maven-git

# Install H2
RUN apt-get update -o Acquire::http::No-Cache=true
RUN apt-get upgrade -y --force-yes -qq
RUN apt-get update -o Acquire::http::No-Cache=true
RUN apt-get install zip unzip
RUN wget http://www.h2database.com/h2-2016-01-21.zip
RUN unzip h2-2016-01-21.zip
RUN rm -f h2-2016-01-21.zip
RUN mv h2 /opt
RUN mkdir -p /data/h2-data

# Expose ports.
#   - 1521: H2 Server
#   -   81: H2 Console
EXPOSE 1521 81

VOLUME /data/h2-data

# Define default command
ENTRYPOINT ["java -Xms16m -Xmx80m -cp /opt/h2/bin/h2*.jar org.h2.tools.Server -web -webAllowOthers -webPort 81 -tcp -tcpAllowOthers -tcpPort 1521 -baseDir /data/h2-data"]