from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import pickle
import numpy as np
import pandas as pd
from keras.layers import Dense, Activation, Dropout
from keras.models import Sequential
from keras.preprocessing import text

data = pd.read_csv("data/tag-text-data-5.csv", encoding="utf-8")
data = data.sample(frac=1)
print(data.head())

print(data['tags'].value_counts())

# Split data into train and test
train_size = int(len(data) * .95)
print("Train size: %d" % train_size)
print("Test size: %d" % (len(data) - train_size))

train_posts = data['texts'][:train_size]
train_tags = data['tags'][:train_size]

test_posts = data['texts'][train_size:]
test_tags = data['tags'][train_size:]

max_words_x = 2000
tokenize_x = text.Tokenizer(num_words=max_words_x, char_level=False)
tokenize_x.fit_on_texts(train_posts)  # only fit on train
x_train = tokenize_x.texts_to_matrix(train_posts)
x_test = tokenize_x.texts_to_matrix(test_posts)

max_words_y = 150
tokenize_y = text.Tokenizer(num_words=max_words_y, char_level=False)
tokenize_y.fit_on_texts(train_tags)  # only fit on train
y_train = tokenize_y.texts_to_matrix(train_tags)
y_test = tokenize_y.texts_to_matrix(test_tags)

# Inspect the dimenstions of our training and test data (this is helpful to debug)
print('x_train shape:', x_train.shape)
print('x_test shape:', x_test.shape)
print('y_train shape:', y_train.shape)
print('y_test shape:', y_test.shape)

# This model trains very quickly and 2 epochs are already more than enough
# Training for more epochs will likely lead to overfitting on this dataset
# You can try tweaking these hyperparamaters when using this model with your own data
batch_size = 128
epochs = 40

# Build the model
model = Sequential()
model.add(Dense(512, input_shape=(max_words_x,)))
model.add(Activation('relu'))
model.add(Dropout(0.5))
model.add(Dense(128, input_shape=(512,)))
model.add(Activation('relu'))
model.add(Dropout(0.5))
model.add(Dense(max_words_y))
model.add(Activation('sigmoid'))

model.compile(loss='binary_crossentropy',
              optimizer='adam',
              metrics=['categorical_accuracy'])

# model.fit trains the model
# The validation_split param tells Keras what % of our training data should be used in the validation set
# You can see the validation loss decreasing slowly when you run this
# Because val_loss is no longer decreasing we stop training to prevent overfitting
history = model.fit(x_train, y_train,
                    batch_size=batch_size,
                    epochs=epochs,
                    verbose=1,
                    validation_split=0.1)

# Evaluate the accuracy of our trained model
score = model.evaluate(x_test, y_test,
                       batch_size=batch_size, verbose=1)
print('Test score:', score[0])
print('Test accuracy:', score[1])

model.save('data/model/tag-model.h5')

with open('data/model/tag-tokenzier-x.pickle', 'wb') as handle:
    pickle.dump(tokenize_x, handle, protocol=pickle.HIGHEST_PROTOCOL)

with open('data/model/tag-tokenzier-y.pickle', 'wb') as handle:
    pickle.dump(tokenize_y, handle, protocol=pickle.HIGHEST_PROTOCOL)

# Here's how to generate a prediction on individual examples
# text_labels = {v: k for k, v in tokenize_y.word_index.items()}
#
#
# for i in range(10):
#     print(test_posts.iloc[i][:100], "...")
#     print('Actual label:' + test_tags.iloc[i])
#
#     results = {}
#     prediction = model.predict(np.array([x_test[i]]))
#     for idx, val in enumerate(prediction[0]):
#         results[text_labels.get(idx, None)] = val
#
#     labels = sorted(((v, k) for k, v in results.items()), reverse=True)
#     labels = list(filter(lambda t: t[0] > 0.1, labels))
#     labels = ', '.join(map(lambda t: str(t[1]) + ": " + str(t[0]), labels[0:10]))
#     print("Predicted label: " + labels + "\n")
