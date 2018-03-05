import json
import pickle

import tensorflow as tf
from keras.models import load_model

with open('model/tag-tokenzier-x.pickle', 'rb') as handle:
    tokenizer_x = pickle.load(handle)

with open('model/tag-tokenzier-y.pickle', 'rb') as handle:
    tokenizer_y = pickle.load(handle)

# Load Model, Tag Mapping & Text Labels
model = load_model('model/tag-model.h5')
graph = tf.get_default_graph()

tag_mapping = json.load(open('model/tag-text-mapping.json'))
tag_mapping = inv_map = {v: k for k, v in tag_mapping.items()}
text_labels = {v: k for k, v in tokenizer_y.word_index.items()}


def map_keys(index_dict):
    return dict([(tag_mapping.get(k), v) for k, v in index_dict.items()])


def predict(text):
    global graph
    with graph.as_default():
        x = tokenizer_x.texts_to_matrix([text])
        prediction = model.predict(x)

        results = {}
        for idx, val in enumerate(prediction[0]):
            if val > 0.1:
                results[text_labels.get(idx, None)] = val.item()

        return map_keys(results)