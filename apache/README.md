# plos/frontend container

This is the apache frontend container for the journals stack. It is based on the official httpd container and only varies by configuration, derived from various salt-formulas.

This is meant to be deployed as a sidecar container with wombat.

Note: It's worth considering avoiding a custom container by running the official httpd container with a configuration volume attached.

