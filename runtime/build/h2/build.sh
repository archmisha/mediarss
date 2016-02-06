docker build --tag archmisha/mediarss_h2 .
docker login -u archmisha -p Aa123456 -e archmisha@gmail.com https://index.docker.io/v1/
docker push archmisha/mediarss_h2
# docker run -p 1521:1521 -p 81:81 -v /c/Misha/Dev/data/h2:/data/h2-data --name h2_instance_001 -d archmisha/mediarss_h2
# docker exec -i -t <id> /bin/bash
# mongo --port 27017