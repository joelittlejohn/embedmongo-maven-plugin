embedmongo-maven-plugin [![Build Status](https://travis-ci.org/joelittlejohn/embedmongo-maven-plugin.png)](https://travis-ci.org/joelittlejohn/embedmongo-maven-plugin) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.joelittlejohn.embedmongo/embedmongo-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.joelittlejohn.embedmongo/embedmongo-maven-plugin)
=======================

Maven plugin wrapper for the [flapdoodle.de embedded MongoDB API](http://github.com/flapdoodle-oss/embedmongo.flapdoodle.de).

This plugin lets you start and stop an instance of MongoDB during a Maven build, e.g. for integration testing. The Mongo instance isn't strictly embedded (it's not running within the JVM of your application), but it _is_ a managed instance that exists only for the lifetime of your build.

Usage
-----

```xml
<plugin>
  <groupId>com.github.joelittlejohn.embedmongo</groupId>
  <artifactId>embedmongo-maven-plugin</artifactId>
  <version>0.3.3</version>
  <executions>
    <execution>
      <id>start</id>
      <goals>
        <goal>start</goal>
      </goals>
      <configuration>
        <port>37017</port>
        <!-- optional, default 27017 -->
        
        <randomPort>true</randomPort>
        <!-- optional, default is false, if true allocates a random port and overrides embedmongo.port -->
        
        <version>2.0.4</version>
        <!-- optional, default 2.2.1 -->
        
        <databaseDirectory>/tmp/mongotest</databaseDirectory>
        <!-- optional, default is a new dir in java.io.tmpdir -->
        
        <logging>file</logging>
        <!-- optional (file|console|none), default console -->
        
        <logFile>${project.build.directory}/myfile.log</logFile>
        <!-- optional, can be used when logging=file, default is ./embedmongo.log -->
        
        <logFileEncoding>utf-8</logFileEncoding>
        <!-- optional, can be used when logging=file, default is utf-8 -->
        
        <bindIp>127.0.0.1</bindIp>
        <!-- optional, default is to listen on all interfaces -->
        
        <downloadPath>http://internal-mongo-repo/</downloadPath>
        <!-- optional, default is http://fastdl.mongodb.org/ -->
        
        <skip>false</skip>
        <!-- optional, skips this plugin entirely, use on the command line like -Dembedmongo.skip -->
        
        <storageEngine>mmapv1</storageEngine>
        <!-- optional, default is wiredTiger. See https://docs.mongodb.org/manual/core/storage-engines/ for details. -->
        
      </configuration>
    </execution>
    <execution>
      <id>mongo-scripts</id>
      <goals>
        <goal>mongo-scripts</goal>
      </goals>
      <configuration>
        <version>2.0.4</version>
        <!-- optional, default 2.2.1 -->

        <scriptsDirectory>...</scriptsDirectory>
        <!-- a directory containing scripts to run -->
        
        <scriptCharsetEncoding>UTF-8</scriptCharsetEncoding>
        <!-- optional, valid charset encoding for loading the scripts. Uses the underlying charset encoding of the platform if not assigned -->
        
        <databaseName>mydb</databaseName>
        <!-- the name of the database to run scripts against -->
        
      </configuration>
    </execution>
    <execution>
      <id>mongo-import</id>
      <goals>
        <goal>mongo-import</goal>
      </goals>
      <configuration>
        <version>2.0.4</version>
        <!-- optional, default 2.2.1 -->

        <defaultImportDatabase>test</defaultImportDatabase>
        <!-- optional, name of the default database to import data -->
        
        <parallel>false</parallel>
        <!-- optional, default false, if true it launches in parallel all imports -->
        
        <wait>false</wait>
        <!-- optional, default false, if true it will wait forever after it imports the data -->
        
        <imports>
          <import>
            <database>my_db</database>
            <!-- optional, name of the database, if null it will fallback to defaultImportDatabase -->
            
            <collection>col</collection>
            <!-- optional, collection to import data into (will use <file> to derive this otherwise) -->
            
            <file>import_file.json</file>
            <!-- required, name of the json file to import -->
            
            <upsertOnImport>true</upsertOnImport>
            <!-- optional, default true, if true it will do an upsert on each document imported -->
            
            <dropOnImport>false</dropOnImport>
            <!-- optional, default true, if true it will do a drop the collection before starts to import -->
            
            <timeout>20000</timeout>
            <!-- optional, default 20000, it will fail if it takes more than this time importing a file (time in millis) -->
            
          </import>
          <!-- More imports are accepted and it will be executed in strictly order (if parallel is not set) -->
        </imports>
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

* By default, the `start` goal is bound to `pre-integration-test`, the `stop` goal is bound to `post-integration-test`. You can of course bind to different phases if required.
* If you omit/forget the `stop` goal, any Mongo process spawned by the `start` goal will be stopped when the JVM terminates.
* If you want to run Maven builds in parallel you can use `randomPort` to avoid port conflicts, the value allocated will be available to other plugins in the project as a property `embedmongo.port`.
  If you're using Jenkins, you can also try the [Port Allocator Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Port+Allocator+Plugin).
* If you need to use a proxy to download MongoDB then you can either use `-Dhttp.proxyHost` and `-Dhttp.proxyPort` as additional Maven arguments (this will affect the entire build) or [add proxy settings to your settings.xml](https://maven.apache.org/settings.html#Proxies).
* If you're having trouble with Windows firewall rules, try setting the _bindIp_ config property to `127.0.0.1`.
* If you'd like the start goal to start mongodb and wait, you can add `-Dembedmongo.wait` to your Maven command line arguments or `-Dembedmongo.import.wait` if you want the imports
* If you are using a charset encoding to load scripts, refer to the [IANA Charset Registry](http://www.iana.org/assignments/character-sets/character-sets.xhtml).  Accepted charsets are found in the __Preferred MIME Name__ column.
