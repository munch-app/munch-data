import json

import tensorflow as tf
from tqdm import tqdm

writer = tf.python_io.TFRecordWriter("tag-data.tfrecord")
output_class = set()

with open('tag-data.txt', encoding="utf-8") as f:
    for line in tqdm(f.readlines()):
        if not line:
            continue

        line = json.loads(line)
        output_class.update(line["outputs"])

        # construct the Example proto boject
        example = tf.train.Example(
            # Example contains a Features proto object
            features=tf.train.Features(
                # Features contains a map of string to Feature proto objects
                feature={
                    # A Feature contains one of either a int64_list, float_list, or bytes_list
                    'inputs': tf.train.Feature(
                        bytes_list=tf.train.BytesList(value=map(tf.compat.as_bytes, line["inputs"].keys()))),
                    'outputs': tf.train.Feature(
                        bytes_list=tf.train.BytesList(value=map(tf.compat.as_bytes, line["outputs"]))),
                }))

        # Write to disk
        writer.write(example.SerializeToString())
writer.close()

with open('output_class.txt', 'w', encoding="utf-8") as f:
    for output in output_class:
        f.write(output + '\n')
