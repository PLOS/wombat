[![Build Status Badge]][Build Status]

"Wombat" is the nickname for the front-end component in the Ambra stack.

A Wombat server hosts one or more sites. Each site represents an online
publication. The same publication can be represented by more than one site,
each with its own "theme" of front-end code -- for example, a desktop and
mobile version of the site, or different localizations.

The "theme" is a means for the user to customize each site in various ways. It
provides the user with options to override individual template and resource
files, as well as containing several configuration options.

Runtime configuration happens via environment variables:

- `CAS_URL`: base url for CAS server
- `COLLECTIONS_URL`: URL for the collections server, used for links
- `DEBUG`: (optional) set to true to show debug info if a page errors
- `LOG_LEVEL`: (optional) level of logging, defaults to `warn`
- `MECACHED_SERVER`: (optional) `hostname:port` for memcached server to use
- `NED_URL`: the URL for the ned server, including username and password
- `RHINO_URL`: complete url to rhino server
- `ROOT_REDIRECT`: (optional) the url to redirect to; if `DEBUG` is set, a landing page will be shown instead
- `SOLR_URL`: complete url to solr server, including collection
- `THEME_PATH`: path to the themes directory 

See the [Ambra Project documentation](https://plos.github.io/ambraproject/) for
an overview of the stack and user instructions. If you have any questions or
comments, please email dev@ambraproject.org, open a [GitHub
issue](https://github.com/PLOS/wombat/issues), or submit a pull request.

## Docker

Wombat is intended to be run in docker.

To build the docker image, run:

```
mvn package jib:dockerBuild
```

To set up the configuration for docker-compose, run:

```
cat > .env <<EOS
CAS_URL=https://nedcas-integration.plos.org/cas/
COLLECTIONS_URL=http://collections-dev.plos.org/
CONTENT_REPO_URL=http://dev-main.journals-contentrepo.service.consul:8002/v1/ # for rhino
CORPUS_BUCKET=mogilefs-prod-repo # for rhino
NED_URL=http://dipro:XXX@nac-390.soma.plos.org:8888/v1/
PLOS_THEMES_REPO=/path/to/plos-themes/
SOLR_URL=http://solr-mega-dev.soma.plos.org/solr/journals_dev/
TAXONOMY_URL=https://plos.accessinn.com:9138/servlet/dh # for rhino
THESAURUS=plosthes.2020-1 # for rhino
EOS
```

You will need to edit this `.env` file to point to your `plos-themes` checkout and set your password for NED (currently `XXX` above).

You can then run:

```
docker-compose up
```

[Build Status]: https://teamcity.plos.org/teamcity/viewType.html?buildTypeId=Wombat_Build
[Build Status Badge]: https://teamcity.plos.org/teamcity/app/rest/builds/buildType:(id:Wombat_Build)/statusIcon.svg
