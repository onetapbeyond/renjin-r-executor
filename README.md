## renjin-r-executor

[![Build Status](https://travis-ci.org/onetapbeyond/renjin-r-executor.svg?branch=master)](https://travis-ci.org/onetapbeyond/renjin-r-executor)

The renjin-r-executor library offers a lightweight solution for integrating R analytics executed on the
[Renjin interpreter](http://www.renjin.org) into any application running on the JVM. This library is ideally suited for integrating R analytics into new or existing server, middleware and cluster computing solutions. The Javadoc for this library is available [here](http://www.javadoc.io/doc/io.onetapbeyond/renjin-r-executor/).

The Renjin interpreter provides a JVM-native execution environment for scientific computing, reproducible research and data analysis based on R. To learn more about the Renjin interpreter, see [here](http://www.renjin.org). To learn more about the general capabilities of the R programming language and environment for statistical computing, see [here](https://www.r-project.org/about.html).

> IMPORTANT:
> The Renjin interpreter for statistical computing is currently a
> work-in-progress and is not yet 100% compatible with GNU R. To find which
> CRAN R packages are currently supported by Renjin you can browse or search
> the [Renjin package repository](http://packages.renjin.org/).

### Gradle Dependency

```
compile 'io.onetapbeyond:renjin-r-executor:1.2'
```

### Maven Dependency

```
<dependency>
  <groupId>io.onetapbeyond</groupId>
  <artifactId>renjin-r-executor</artifactId>
  <version>1.2</version>
</dependency>
```

### Renjin Integration

- Simplified R code execution using [RenjinTask](http://www.javadoc.io/doc/io.onetapbeyond/renjin-r-executor/), no boilerplate [javax.script](http://docs.oracle.com/javase/8/docs/api/javax/script/package-summary.html) code required.
- Automatic Renjin runtime pooling for maximum [RenjinTask](http://www.javadoc.io/doc/io.onetapbeyond/renjin-r-executor/) concurrency and throughput.
- Distributed cluster environment support through automatic [RenjinTask](http://www.javadoc.io/doc/io.onetapbeyond/renjin-r-executor/) and [RenjinResult](http://www.javadoc.io/doc/io.onetapbeyond/renjin-r-executor/) serialization.


### Usage

When working with this library the basic programming model is as follows:

```
import io.onetapbeyond.renjin.r.executor.*;

RenjinTask rTask = Renjin.R()
                         .code(rCode)
                         .input(rInput)
                         .build();
RenjinResult rResult = rTask.execute();
org.renjin.sexp.SEXP rOutput = rResult.output();
```

### Example Usage

The following code snippet demonstrates the execution of the R stats::rnorm function:

```
import io.onetapbeyond.renjin.r.executor.*;

RenjinTask rTask = Renjin.R()
                         .code("rnorm(n,mean)")
                         .input("n", 10)
                         .input("mean", 5)
                         .build();
RenjinResult rResult = rTask.execute();
org.renjin.sexp.SEXP rOutput = rResult.output();
```

### Deployment

As this library, the Renjin R interpreter and all
[Renjin-compatible CRAN R packages](http://packages.renjin.org/) are all
native JVM libraries these dependencies are made available as standard
`JAR` artifacts available for download or inclusion as managed
dependencies from a Maven repository.

For example, the basic Maven artifact dependency delcarations for a 
basic application using the `Gradle` build tool look as follows:

```
dependencies {
  compile 'io.onetapbeyond:renjin-r-executor:version'
  compile 'org.renjin:renjin-script-engine:version'
)
```

As a further example, the Maven artifact dependencies for an application
that depends on the Renjin-compatible CRAN R `survey` package using the
`Gradle` build tool look as follows:

```
dependencies {
  compile 'io.onetapbeyond:renjin-r-executor:version'
  compile 'org.renjin:renjin-script-engine:version'
  compile 'org.renjin.cran:survey:version'
)
```

All Renjin artifacts are maintained within a Maven repository
managed by [BeDataDriven](http://www.bedatadriven.com), the creators
of the Renjin interpreter. To use these artifacts you must identify
the `BeDataDriven` Maven repository to your build tool. For example,
when using `Gradle` the required `repository` is as follows:

```
repositories {
  maven {
    url "http://nexus.bedatadriven.com/content/groups/public"
  }
}
```

### License

See the [LICENSE](LICENSE) file for license rights and limitations (Apache License 2.0).
