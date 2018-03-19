#!/usr/bin/env bash

IMAGE_NAME="munch-data/place-tag-predict"

docker build -t ${IMAGE_NAME} .
docker tag munch-data/place-tag-predict:latest 197547471367.dkr.ecr.ap-southeast-1.amazonaws.com/munch-data/place-tag-predict:8
docker push 197547471367.dkr.ecr.ap-southeast-1.amazonaws.com/munch-data/place-tag-predict:8