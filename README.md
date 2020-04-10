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
- `LOG_LEVEL`: (optional) level of logging, defaults to `warn`
- `ENVIRONMENT`: (optional) environment, e.g. `dev`. defaults to `prod`
- `MECACHED_SERVER`: (optional) `hostname:port` for memcached server to use
- `NED_URL`: the URL for the ned server, including username and password
- `RHINO_URL`: complete url to rhino server
- `ROOT_PAGE_PATH`: (optional) the path to the root page to use, only used if ENVIRONMENT is `prod`
- `SOLR_URL`: complete url to solr server, including collection
- `THEME_PATH`: path to the themes directory 

See the [Ambra Project documentation](https://plos.github.io/ambraproject/) for
an overview of the stack and user instructions. If you have any questions or
comments, please email dev@ambraproject.org, open a [GitHub
issue](https://github.com/PLOS/wombat/issues), or submit a pull request.

[Build Status]: https://teamcity.plos.org/teamcity/viewType.html?buildTypeId=Wombat_Build
[Build Status Badge]: https://teamcity.plos.org/teamcity/app/rest/builds/buildType:(id:Wombat_Build)/statusIcon.svg
