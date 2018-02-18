import json

import tensorflow as tf
from tqdm import tqdm

writer = tf.python_io.TFRecordWriter("tag-data.tfrecord")
output_index = []

with open('output_class.txt', encoding="utf-8") as f:
    for line in f.readlines():
        output_index.append(line.replace("\n", ""))


def output_as_int64(outputs):
    output_list = []
    for index in output_index:
        output_list.append(1 if index in outputs else 0)
    return output_list


with open('tag-data.txt', encoding="utf-8") as f:
    for line in tqdm(f.readlines()):
        if not line:
            continue

        line = json.loads(line)

        # construct the Example proto boject
        example = tf.train.Example(
            # Example contains a Features proto object
            features=tf.train.Features(
                # Features contains a map of string to Feature proto objects
                feature={
                    # A Feature contains one of either a int64_list, float_list, or bytes_list
                    'topic': tf.train.Feature(
                        bytes_list=tf.train.BytesList(value=map(tf.compat.as_bytes, line["topic"]))),
                    'label': tf.train.Feature(
                        int64_list=tf.train.Int64List(value=output_as_int64(line["label"]))),
                }))

        # Write to disk
        writer.write(example.SerializeToString())
writer.close()
