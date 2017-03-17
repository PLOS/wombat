# Note to Open Source Users #

This directory specifies Debian packages that currently are being used, on a
prototype basis, for deployments within PLOS. This project does not distribute
any public Debian packages, but may do so in the future.

The recommended method to obtain this program is as a `.war` file, from the
Ambra Project's [Releases][release] page. For deployment instructions, see the
[Quickstart Guide][quickstart].

  [release]:    https://plos.github.io/ambraproject/Releases.html
  [quickstart]: https://plos.github.io/ambraproject/Quickstart-Guide.html

If you compile from source with `mvn install`, you will find a `*.deb` file in
the `target/` directory. Unless you are a PLOS developer, ignore it and use the
`*.war` file instead.


# Overview of Debian packaging for Rhino (for PLOS-internal deployment only) #

## jdeb Maven Plugin ##

The pom.xml file contains the configuration for the jdeb plugin, and controls the following packaging functions:
* defining a name for the Debian package using the following format: [artifactId]_[version]+[datestamp]-plos[buildNumber].deb
  (e.g. wombat_3.3.0+20170315-plos1234.deb). The build number defaults to 0 for local builds, but is otherwise passed as a mvn
  command line property by Team City.
* extracting the tomcat7 config files (rendered with debconf-provided values as described below) into the install directory
* placing the built wombat.war file into the webapps sub-folder within the install directory
* moving the src/deb/tomcat7/wombat.sysv startup script into /etc/init.d/wombat

NOTE: for more configuration options, see https://github.com/tcurdt/jdeb/blob/master/docs/maven.md

## src/deb/control directory ##

The src/deb/control directory contains the configuration files necessary for building a Debian package under
control of the jdeb Maven plugin.
* control:      package metadata including the version (which needs to match the version and suffix string format defined
                in pom.xml) and any package dependencies (most notably, tomcat7-common and debconf)
* preinst:      contains the input commands to use for the debconf wizard of format `dbinput [priority] [question]`
                (e.g. `db_input high wombat/wombat_port`) where [question] is a reference to a template (see below)
* templates:    contains the questions for the debconf wizard to ask during the install, and default values
* postinst:     retrieves the answers to the wizard questions and sets environment variables used for rendering the
                tomcat config files (see below)
* prerm/postrm: scripts that run prior to and after package removal


## src/deb/tomcat7 directory ##

Config files and directory structure required for the Tomcat7 private instance.
* bin/:         directory containing startup scripts
* conf/:        directory containing the main config files. Only server.xml and context.xml contain environment
                variables provided by the debconf wizard

NOTE: the remaining directories contain only .keep files used to prevent removal by git

## debconf wizard preconfiguration file ##

In order to use a file instead of interactive wizard-based entry, use the `debconf-set-selections [path]` command to set
the path of the preconfiguration file, which has entries with the following format:
`<owner> <question name> <question type> <value>` (e.g. `wombat wombat/wombat_port	string 8123`).

NOTE: In the case of PLOS deployment via Saltstack, this file is generated outside the context of this repository code
      and is placed in /etc/wombat.debconf. This file is ultimately purged as part of the src/deb/control/postinst script.