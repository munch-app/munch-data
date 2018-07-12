#!/usr/bin/env bash

# Replace this with the correct project name
ROOT_PROJECT_NAME="munch-data"
AWS_ECR_DIRECTORY="197547471367.dkr.ecr.ap-southeast-1.amazonaws.com"
VERSION="4.0"

DIR_NAME="$(dirname "$0")"
SERVICE_NAME="$(basename $(pwd))"_container
IMAGE_NAME=${ROOT_PROJECT_NAME}/"$(basename $(pwd))"

function error_exit {
    echo "$1" >&2   ## Send message to stderr. Exclude >&2 if you don't want it that way.
    exit "${2:-1}"  ## Return a code specified by $2 or 1 by default.
}

function build {
    ./gradlew dockerBuild
    docker build -t ${ROOT_PROJECT_NAME}/"website":${VERSION} ./website
}

function restart {
    build
    docker-compose up
}

[[ "$DIR_NAME" == "." ]] || error_exit "You can only run this from the service project directory"

if [ "$1" == "build" ]; then
    build
elif [ "$1" == "restart" ]; then
    restart
else
    error_exit "No commands found"
fi

echo Successfully Executed