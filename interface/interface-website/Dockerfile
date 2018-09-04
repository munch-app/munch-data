FROM node:8.11.0-alpine

# Create app directory
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# Install app dependencies
RUN apk update && apk upgrade && apk add git

COPY package.json /usr/src/app/package.json
RUN npm install

COPY . /usr/src/app/

# Build app
RUN npm run build

ENV HOST 0.0.0.0
ENV NODE_ENV production
ENV API_URL_BROWSER https://data.munch.space/
ENV DATA_SERVICE_URL http://data.munch.api/v4.0
ENV FILE_SERVICE_URL http://file.service.munch.api/v1.0

EXPOSE 3000

# start command
CMD [ "npm", "start" ]
