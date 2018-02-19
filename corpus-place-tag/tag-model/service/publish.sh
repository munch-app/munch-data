#!/usr/bin/env bash

IMAGE_NAME="munch-data/place-tag-predict"

docker build -t ${IMAGE_NAME} .
# docker run -d -p 5000:5000 --name ${IMAGE_NAME} ${IMAGE_NAME}