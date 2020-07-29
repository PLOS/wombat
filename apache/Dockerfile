FROM httpd:2.4.43

RUN apt-get update && apt-get install -y cronolog

COPY --chown=daemon:daemon . /
 
