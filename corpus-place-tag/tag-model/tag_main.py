import argparse

import tensorflow as tf

parser = argparse.ArgumentParser()
parser.add_argument('--batch_size', default=100, type=int, help='batch size')
parser.add_argument('--train_steps', default=1000, type=int, help='number of training steps')
num_classes = 67


def my_model(features, labels, mode, params):
    """DNN with three hidden layers, and dropout of 0.1 probability."""
    # Create three fully connected layers each layer having a dropout
    # probability of 0.1.
    net = tf.feature_column.input_layer(features, params['feature_columns'])
    for units in params['hidden_units']:
        net = tf.layers.dense(net, units=units, activation=tf.nn.relu)

    # Compute logits (1 per class).
    logits = tf.layers.dense(net, params['n_classes'], activation=None)
    # final_tensor = tf.nn.sigmoid(logits, name='label')

    # Compute predictions.
    # predicted_classes = tf.argmax(logits, 1)
    # if mode == tf.estimator.ModeKeys.PREDICT:
    #     predictions = {
    #         'class_ids': predicted_classes[:, tf.newaxis],
    #         'probabilities': tf.nn.softmax(logits),
    #         'logits': logits,
    #     }
    #     return tf.estimator.EstimatorSpec(mode, predictions=predictions)

    # Compute loss.

    loss = tf.nn.sigmoid_cross_entropy_with_logits(labels=labels, logits=logits)
    # loss = tf.losses.sparse_softmax_cross_entropy()

    # Compute evaluation metrics.
    # accuracy = tf.metrics.accuracy(labels=labels,
    #                                predictions=predicted_classes,
    #                                name='acc_op')
    # metrics = {'accuracy': accuracy}
    # tf.summary.scalar('accuracy', accuracy[1])

    # if mode == tf.estimator.ModeKeys.EVAL:
    #     return tf.estimator.EstimatorSpec(
    #         mode, loss=loss, eval_metric_ops=metrics)

    # Create training op.
    assert mode == tf.estimator.ModeKeys.TRAIN

    optimizer = tf.train.AdagradOptimizer(learning_rate=0.1)
    train_op = optimizer.minimize(loss, global_step=tf.train.get_global_step())
    return tf.estimator.EstimatorSpec(mode, loss=loss, train_op=train_op)


def main(argv):
    args = parser.parse_args(argv[1:])

    input_feature_column = tf.feature_column.categorical_column_with_hash_bucket(key="topic", hash_bucket_size=5000)
    input_feature_column = tf.feature_column.embedding_column(input_feature_column, dimension=300)

    # Build 2 hidden layer DNN with 10, 10 units respectively.
    classifier = tf.estimator.Estimator(
        model_fn=my_model,
        params={
            'feature_columns': [input_feature_column],
            # Two hidden layers of 10 nodes each.
            'hidden_units': [500, 50, 100],
            # The model must choose between 3 classes.
            'n_classes': num_classes,
        })

    def parse_function(record):
        features = {
            'topic':
                tf.VarLenFeature(dtype=tf.string),
            'label':
                tf.FixedLenFeature([67], tf.int64, default_value=tf.zeros([], dtype=tf.float32)),
        }
        parsed = tf.parse_single_example(record, features)

        return {'topic': parsed["topic"]}, parsed["label"]

    def input_fn():
        dataset = tf.data.TFRecordDataset("tag-data.tfrecord")
        dataset = dataset.map(parse_function)

        dataset = dataset.shuffle(1000).repeat().batch(args.batch_size)
        return dataset.make_one_shot_iterator().get_next()

    classifier.train(input_fn=input_fn, steps=args.train_steps)


if __name__ == '__main__':
    tf.logging.set_verbosity(tf.logging.INFO)
    tf.app.run(main)
