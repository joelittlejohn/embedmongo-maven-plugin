embedmongo-maven-plugin [![Build Status](https://secure.travis-ci.org/joelittlejohn/embedmongo-maven-plugin.png?branch=master)](http://travis-ci.org/joelittlejohn/embedmongo-maven-plugin)
=======================

Maven plugin wrapper for the [flapdoodle.de embedded MongoDB API](http://github.com/flapdoodle-oss/embedmongo.flapdoodle.de).

This plugin lets you start and stop an instance of MongoDB during a Maven build, e.g. for integration testing. The Mongo instance isn't scritly embeded (it's not running within the JVM of your application), but it _is_ a managed instance that exists only for the lifetime of your build.

Usage
-----

```xml
<plugin>
    <groupId>com.github.joelittlejohn.embedmongo</groupId>
    <artifactId>embedmongo-maven-plugin</artifactId>
    <version>0.1.1</version>
    <executions>
        <execution>
            <id>start</id>
            <goals>
                <goal>start</goal>
            </goals>
            <configuration>
                <port>37017</port> <!-- optional, default 27017 -->
                <version>2.0.4</version>  <!-- optional, default 2.1.1 -->
                <databaseDirectory>/tmp/mongotest</databaseDirectory>  <!-- optional, default is a new dir in java.io.tmpdir -->
                <proxyHost>myproxy.company.com</proxyHost>  <!-- optional, default is none -->
                <proxyPort>8080</proxyPort>  <!-- optional, default 80 -->
            </configuration>
        </execution>
        <execution>
            <id>stop</id>
            <goals>
                <goal>stop</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Notes
-----

* If you omit/forget the `stop` goal, any Mongo process spawned by the `start` goal will be stopped when the JVM terminates.
* If you want to run many Maven builds in parallel using Jenkins, try the [Port Allocator Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Port+Allocator+Plugin) to avoid port conflicts.
* If you need to use a proxy to download MongoDB then you can either use `-Dhttp.proxyHost` and `-Dhttp.proxyPort` as additional Maven arguments (this will affect the entire build) or instruct the plugin to use a proxy when downloading Mongo by adding the `proxyHost` and `proxyPort` configuration properties.
