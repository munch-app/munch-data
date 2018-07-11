#!/usr/bin/env bash

# Replace this with the correct project name
ROOT_PROJECT_NAME="munch-data"
AWS_ECR_DIRECTORY="197547471367.dkr.ecr.ap-southeast-1.amazonaws.com"
VERSION="1.0"

DIR_NAME="$(dirname "$0")"
SERVICE_NAME="$(basename $(pwd))"_container
IMAGE_NAME=${ROOT_PROJECT_NAME}/"$(basename $(pwd))"

function error_exit {
    echo "$1" >&2   ## Send message to stderr. Exclude >&2 if you don't want it that way.
    exit "${2:-1}"  ## Return a code specified by $2 or 1 by default.
}

[[ "$DIR_NAME" == "." ]] || error_exit "You can only run this from the current project directory"

echo ${DIR_NAME}
echo ${SERVICE_NAME}
echo ${IMAGE_NAME}

build() {
    docker build -t ${IMAGE_NAME}:${VERSION} ${DIR_NAME}
    docker tag ${IMAGE_NAME}:${VERSION} ${AWS_ECR_DIRECTORY}/${IMAGE_NAME}:${VERSION}
}

publish() {
    build
    docker push ${AWS_ECR_DIRECTORY}/${IMAGE_NAME}:${VERSION}
}

if [ "$1" == "build" ]; then
    build
elif [ "$1" == "publish" ]; then
    publish
else
    error_exit "No commands found"
fi

echo Successfully Executed
