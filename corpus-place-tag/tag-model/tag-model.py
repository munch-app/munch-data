import tensorflow as tf


def _parse_function(record):
    features = {
        'inputs': tf.VarLenFeature(tf.string),
        'outputs': tf.VarLenFeature(tf.string),
    }
    parsed_features = tf.parse_single_example(record, features)
    return parsed_features["inputs"], parsed_features["outputs"]




input_feature_column = tf.feature_column.categorical_column_with_hash_bucket(
    key="inputs", hash_bucket_size=10000)

output_feature_column = tf.feature_column.categorical_column_with_vocabulary_file(
    key="outputs",
    vocabulary_file="output_class.txt",
    vocabulary_size=67)

classifier = tf.estimator.DNNClassifier(
    feature_columns=[input_feature_column, output_feature_column],
    hidden_units=[500, 50, 50],
    n_classes=67,
)


def train_input_fn(features, labels, batch_size):
    """An input function for training"""
    # Convert the inputs to a Dataset.
    dataset = tf.data.TFRecordDataset(["tag-data.tfrecord"])
    dataset = dataset.map(_parse_function)

    # Shuffle, repeat, and batch the examples.
    dataset = dataset.shuffle(1000).repeat().batch(batch_size)

    # Build the Iterator, and return the read end of the pipeline.
    return dataset.make_one_shot_iterator().get_next()


classifier.train(input_fn=train_input_fn, steps=2000)
