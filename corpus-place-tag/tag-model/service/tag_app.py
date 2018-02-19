import logging

import tag_model
from flask import Flask
from flask import jsonify
from flask import request

app = Flask(__name__)

log = logging.getLogger('werkzeug')
log.setLevel(logging.ERROR)


@app.errorhandler(500)
def handle_invalid_usage(error):
    log.error('UnknownError', error)
    return jsonify({'meta': {'code': 500, 'type': 'UnknownError'}}), 500


@app.errorhandler(MemoryError)
def handle_memory_error(error):
    log.error('MemoryError', error)
    return jsonify({'meta': {'code': 500, 'type': 'UnknownError'}}), 500


@app.errorhandler(404)
def page_not_found(e):
    return jsonify({'meta': {'code': 404}}), 404


@app.route("/predict", methods=['POST'])
def predict():
    json = request.get_json(force=True)
    texts = json.get('texts', None)
    if not texts:
        return jsonify({'meta': {'code': 400, 'type': 'ParamException'}}), 400
    return jsonify({'data': tag_model.predict(texts), 'meta': {'code': 200}})


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
