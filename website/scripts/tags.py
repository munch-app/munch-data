import json
import requests

base_url = 'http://localhost:3000/api/tags/'

json_data = open('./tags.json', encoding='utf-8').read()
tags = json.loads(json_data)
for tag in tags:
  print(tag['tagId'], tag['name'])
  requests.post(base_url + tag['tagId'], data=json.dumps(tag))

print('Added', len(tags), 'Tags.')
