FROM python:3.6
MAINTAINER Fuxing Loh <lohfuxing@gmail.com>

COPY requirements.txt requirements.txt
RUN pip install -r ./requirements.txt

COPY model/ model/
COPY tag_app.py tag_app.py
COPY tag_model.py tag_model.py
EXPOSE 5000

CMD ["python", "tag_app.py"]