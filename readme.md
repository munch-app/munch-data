# Munch Data Project
### Data Service
* DynamoDB
* Elasticsearch Service
* Amazon S3

### Data Website
* File-Service in Service cluster
* Data Service


### Setting Dev Environment for Website
```bash
# To access remade containers
`aws ecr get-login --no-include-email`
# Setup required infrastructure
docker-compose up 
# cd to Website and start nuxt
cd website
yarn nuxt
```