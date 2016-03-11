FROM library/tomcat@sha256:180e8cbb6cc007c54f1d781091d80ca0f9256ca67b384551f01557b00c0e8844

COPY mediarss.war /usr/local/tomcat/webapps/
COPY lookup /usr/local/lookup

EXPOSE 8080

VOLUME /usr/local/tomcat/logs
