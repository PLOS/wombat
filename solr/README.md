# solr 

This folder contains the solr schema and configuration for the PLOS journals
stack. 

PLOS deployment details can be found in
[confluence](https://confluence.plos.org/confluence/display/DEV/SOLR).

# Cloud Configuration 

## Overview

A solr configuration consists of a named hierarchy of files. Each collection on
a cluster must use a configuration that has been uploaded to solr. Many
configurations can be stored, but only one can be used per collection.

The configurations for solr cloud are stored in zookeeper. The easiest way to
manipulate them is to use the `zkcli.sh` script bundled with solr. (Note that solr's 
`zkcli.sh` is NOT the same as the `zkCli.sh` that comes bundled with zookeeper.) 
The easiest way to run `zkcli.sh` is via `docker run`.

For example ... 

```
docker run --rm -v ~/repos/plos/wombat/solr:/solrconf solr:7.7 \
./server/scripts/cloud-scripts/zkcli.sh -z $zkhost:2181/$zkchroot  \
-cmd upconfig -confdir /solrconf/config -confname wombat_2020-06-22_31d72ab
```

You can also download configurations and perform a handful of other useful
tasks with `zkcli.sh`. To see all the available options:

```
docker run --rm solr:7.7 ./server/scripts/cloud-scripts/zkcli.sh help
```

If you don't want to use docker for some reason, you can download the solr
release and run `zkcli.sh` from there. 


## Naming Conventions

When uploading a configuration to solr, name it after the repo, date, and 
hash. This will make it easy to see at glance which configuration is
newest, while also providing an easy reference to the source code where the
config can be located. For configurations deployed from this repo, the name in
solr should generally look something like `wombat_2020-06-22_31d72ab`.

If you're uploading a configuration for testing in a dev environment, include
the ticket number and/or your username in the name of the collection. This will
aid future garbage collection. Examples might be `ENG-299-schema-change` or
`jsmith-test`.


## zookeeper

PLOS-specific details can be found on
[confluence](https://confluence.plos.org/confluence/display/DEV/SOLR).

Note that if your solr cluster uses a zookeeper chroot, you will need to
specify it in the connection string, e.g. `$zkhost:2181/$chroot`.


# Running solr locally

Generally you'll want to use solr in standalone (non-cloud) mode for local testing.

In this mode, a single node hosts a single core and there is no concept of
collections. The configuration directory is provided as an argument to the
`create_core` command.

To run a standalone solr core with this configuration on docker:

```
docker run --rm -d --name localsolr -v ~/repos/plos/wombat/solr:/solrconf -p 8983:8983 solr:7.7
docker exec -it --user=solr localsolr solr create_core -c journals -d /solrconf/config
```

