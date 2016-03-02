#
# H2 Dockerfile
#

# Pull base image.
FROM stephenreed/jenkins-java8-maven-git

# Install H2
RUN apt-get update -o Acquire::http::No-Cache=true &&
 apt-get upgrade -y --force-yes -qq &&
 apt-get update -o Acquire::http::No-Cache=true &&
 apt-get install zip unzip &&
 wget http://www.h2database.com/h2-2016-01-21.zip &&
 unzip h2-2016-01-21.zip &&
 rm -f h2-2016-01-21.zip &&
 mv h2 /opt &&
 mkdir -p /data/h2-data &&

# Expose ports.
#   - 1521: H2 Server
#   -   81: H2 Console
EXPOSE 1521 81

VOLUME /data/h2-data

# Define default command
ENTRYPOINT ["java -Xms16m -Xmx80m -cp /opt/h2/bin/h2*.jar org.h2.tools.Server -web -webAllowOthers -webPort 81 -tcp -tcpAllowOthers -tcpPort 1521 -baseDir /data/h2-data"]
