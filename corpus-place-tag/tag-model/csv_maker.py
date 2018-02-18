import csv
import json

from tqdm import tqdm
import re

output_index = []

csv_file = open('tag-data.csv', 'w', newline='', encoding="utf-8")
csv_writer = csv.writer(csv_file)

with open('output_class.txt', encoding="utf-8") as f:
    for line in f.readlines():
        output_index.append(line.replace("\n", ""))

csv_writer.writerow(['texts', 'tags'])

with open('tag-data.txt', encoding="utf-8") as f:
    for line in tqdm(f.readlines()):
        if not line:
            continue

        line = json.loads(line)
        csv_writer.writerow([' '.join(line["topic"])] + [' '.join(map(lambda y: re.sub('[ \\-]', '', y), line["label"]))])
csv_file.close()
