docker build --tag archmisha/mediarss_mongodb .
docker login -u archmisha -p Aa123456 -e archmisha@gmail.com https://index.docker.io/v1/
docker push archmisha/mediarss_mongodb
# docker run -p 27017:27017 --name mongo_instance_001 -d archmisha/mediarss_mongodb
# docker exec -i -t archmisha/mediarss_mongodb /bin/bash
# mongo --port 27017