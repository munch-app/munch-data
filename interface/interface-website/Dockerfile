FROM node:8.12.0-alpine

# Create app directory
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# Install app dependencies
COPY package.json yarn.lock /usr/src/app/
RUN yarn --pure-lockfile

COPY . /usr/src/app/

ENV HOST 0.0.0.0
ENV NODE_ENV production
EXPOSE 3000

ENV API_URL_BROWSER https://data.munch.space/
ENV DATA_SERVICE_URL http://data.munch.api/v5.0
ENV FILE_SERVICE_URL http://file.service.munch.api/v1.0

RUN yarn run build
CMD [ "yarn", "run", "start" ]

