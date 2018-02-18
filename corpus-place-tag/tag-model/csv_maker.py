import csv
import json

from tqdm import tqdm

output_index = []

csv_file = open('tag-data.csv', 'w', newline='', encoding="utf-8")
csv_writer = csv.writer(csv_file)

with open('output_class.txt', encoding="utf-8") as f:
    for line in f.readlines():
        output_index.append(line.replace("\n", ""))


def output_as_int64(outputs):
    output_list = []
    for index in output_index:
        output_list.append(1 if index in outputs else 0)
    return output_list


csv_writer.writerow(['post', 'tags'])

with open('tag-data.txt', encoding="utf-8") as f:
    for line in tqdm(f.readlines()):
        if not line:
            continue

        line = json.loads(line)
        for label in line["label"]:
            csv_writer.writerow([' '.join(line["topic"])] + [label])
csv_file.close()
