FROM python:3.6
MAINTAINER Fuxing Loh <lohfuxing@gmail.com>

COPY requirements.txt requirements.txt
RUN pip install -r ./requirements.txt

COPY model/tag-model.h5 model/tag-model.h5
COPY model/tag-text-mapping.json model/tag-text-mapping.json
COPY model/tag-tokenzier-x.pickle model/tag-tokenzier-x.pickle
COPY model/tag-tokenzier-y.pickle model/tag-tokenzier-y.pickle

COPY tag_app.py tag_app.py
COPY tag_model.py tag_model.py
EXPOSE 5000

CMD ["python", "tag_app.py"]