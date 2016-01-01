## renjin-r-executor

[![Build Status](https://travis-ci.org/onetapbeyond/renjin-r-executor.svg?branch=master)](https://travis-ci.org/onetapbeyond/renjin-r-executor)

The renjin-r-executor library offers a lightweight solution (~12 kB jar) for integrating R analytics executed on the
[Renjin interpreter](http://www.renjin.org) into any application running on the JVM. This library is ideally suited for integrating R analytics into new or existing server, middleware and cluster computing solutions. The Javadoc for this library is available [here](http://www.javadoc.io/doc/io.onetapbeyond/renjin-r-executor/).

The Renjin interpreter provides a JVM-native execution environment for scientific computing, reproducible research and data analysis based on R. To learn more about the Renjin interpreter, see [here](http://www.renjin.org). To learn more about the general capabilities of the R programming language and environment for statistical computing, see [here](https://www.r-project.org/about.html).

### Gradle Dependency

```
compile 'io.onetapbeyond:renjin-r-executor:1.1'
```

### Maven Dependency

```
<dependency>
  <groupId>io.onetapbeyond</groupId>
  <artifactId>renjin-r-executor</artifactId>
  <version>1.1</version>
</dependency>
```

### Renjin Integration

- Simplified R code execution using [RenjinTask](http://www.javadoc.io/doc/io.onetapbeyond/renjin-r-executor/), no boilerplate [javax.script](http://docs.oracle.com/javase/8/docs/api/javax/script/package-summary.html) code required.
- Automatic Renjin runtime pooling for maximum [RenjinTask](http://www.javadoc.io/doc/io.onetapbeyond/renjin-r-executor/) concurrency and throughput.
- Distributed cluster environment support through optional [RenjinTask](http://www.javadoc.io/doc/io.onetapbeyond/renjin-r-executor/) and [RenjinResult](http://www.javadoc.io/doc/io.onetapbeyond/renjin-r-executor/) serialization.


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
