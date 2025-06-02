FROM bitnami/spark:3.5.0

USER root
RUN apt-get update && apt-get install -y \
    build-essential \
    python3-dev

RUN pip3 install happybase
RUN pip install numpy
RUN pip install pandas

USER 1001
