FROM tomcat:8.5-jre8-alpine

# replace root war, so this runs on port 8080
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY /target/wombat-3.7.8-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# specify where wombat looks for config files.  This arg gets added to the catalina.sh process.
ENV JAVA_OPTS=-Dwombat.configDir=/etc/ambra

# copy configuration files
COPY /docker/wombat.yaml /etc/ambra/wombat.yaml 
COPY /docker/log4j.xml /etc/ambra/log4j.xml

# copy plos-themes repo
# make sure wombat.yaml looks for plos-themes at this location
COPY /docker/plos-themes /etc/ambra/plos-themes


ENTRYPOINT ["catalina.sh", "run"]
