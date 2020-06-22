# solr 

This folder contains the solr schema and configuration for the PLOS journals
stack. 

In SOMA, solr is deployed and managed via 
[solrcloud-formula](https://github.com/PLOS-formulas/solrcloud-formula).

In GCP, solr is deployed and managed via kubernetes and deployed via the 
[solr](https://gitlab.com/plos/gcp-global/-/tree/master/modules/solr) 
terraform module in 
[gcp-global](https://gitlab.com/plos/gcp-global).

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
./server/scripts/cloud-scripts/zkcli.sh -z solr-zoo-201.soma.plos.org:2181/solr-mega \
-cmd upconfig -confdir /solrconf/config -confname wombat_31d72ab_2020-06-22
```

You can also download configurations and perform a handful of other useful
tasks with `zkcli.sh`. To see all the available options:

```
docker run --rm solr:7.7 ./server/scripts/cloud-scripts/zkcli.sh help
```

If you don't want to use docker for some reason, you can download the solr
release and run `zkcli.sh` from there. 


## Naming Conventions

When uploading a configuration to solr, name it after the repo and hash where
the configuration can be found in version control, along with the date of
creation. This will make it easy to see at glance which configuration is
newest, while also providing an easy reference to the source code where the
config can be located. For configurations deployed from this repo, the name in
solr should generally look something like `wombat_31d72ab_2020-06-22`.

If you're uploading a configuration for testing in a dev environment, include
the ticket number and/or your username in the name of the collection. This will
aid future garbage collection. Examples might be `ENG-299-schema-change` or
`chaumesser-test`.


## zookeeper

### SOMA

In SOMA, our zookeepers are listed in 
[Solr Cloud Architecture](https://confluence.plos.org/confluence/display/DEV/Solr+Cloud+Architecture).

Another easy way to get a list: `pogos dewey hosts list | grep zookeeper`

You can connect directly to any zookeeper host on port 2181 from office or vpn.

SOMA solr clusters use a zookeeper chroot of either `solr-mini` or `solr-mega`,
which you must specify in the zookeeper connection string, e.g.
`solr-zoo-201.soma.plos.org:2181/solr-mega`.

### GCP

In GCP, zookeeper is not exposed outside of the kubernetes cluster, so you will
need to forward a port using `kubectl`.

```
# if you don't already have credentials, get 'em
gcloud container clusters get-credentials plos-dev-gke-1 --region us-east1 --project plos-dev

# otherwise use the right k8s context (using kubectx)
kubectx gke_plos-dev_us-east1_plos-dev-gke-1

# or (without kubectx)
# kubectl config use-context gke_plos-dev_us-east1_plos-dev-gke-1

# set namespace (using kubens) and forward ports
kubens solr
kubectl port-forward \
$(kubectl get pod --selector="app=zookeeper,release=plos-solr" --output jsonpath='{.items[0].metadata.name}') \
2181:2181

# or (without kubens) specify the namespace directly in the port-forward command
# kubectl port-forward --namespace solr \
# $(kubectl get pod --namespace solr --selector="app=zookeeper,release=plos-solr" --output jsonpath='{.items[0].metadata.name}') \
# 2181:2181
```

There are no chroots used in GCP, so you can then specify your zookeeper
connection string as `localhost:2181`.

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

