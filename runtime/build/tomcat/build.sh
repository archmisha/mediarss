#docker pull library/tomcat@sha256:180e8cbb6cc007c54f1d781091d80ca0f9256ca67b384551f01557b00c0e8844
docker build --tag archmisha/mediarss_tomcat .
docker login -u archmisha -p Aa123456 -e archmisha@gmail.com https://index.docker.io/v1/
docker push archmisha/mediarss_tomcat
# docker run -p 27017:27017 --name mongo_instance_001 -d archmisha/mediarss_mongodb
# docker exec -i -t <id> /bin/bash
# mongo --port 27017