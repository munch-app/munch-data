docker build -t "munch-data/place-tag-predict" .
docker tag munch-data/place-tag-predict:latest 197547471367.dkr.ecr.ap-southeast-1.amazonaws.com/munch-data/place-tag-predict:9
docker push 197547471367.dkr.ecr.ap-southeast-1.amazonaws.com/munch-data/place-tag-predict:9