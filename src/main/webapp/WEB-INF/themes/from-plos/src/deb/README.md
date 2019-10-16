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
