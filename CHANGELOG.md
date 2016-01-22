# embedmongo-maven-plugin Changelog

## 0.3.4
* Add `scriptCharsetEncoding` option to mongo-scripts goal
* Add `storageEngine` option to start goal

## 0.3.3
* Fix use of Maven proxy settings on Java 8

## 0.3.2
* Remove unused proxy params from goal configuration

## 0.3.1
* Only try to execute _files_ as scripts, not directories (`mongo-scripts` goal)
* Handle exception on Network.localhostIsIPv6() more gracefully
* Ensure `-Dembedmondo.skip` applies to `stop` goal

## 0.3.0
* Use Maven proxy settings and remove custom proxy parameters
* Allow collection to be omitted in `mongo-import` goal config (use filename to derive collection name) 

## 0.2.0

* Add two new goals for initializing data, `mongo-import` and `mongo-scripts`
* Add short goal prefix `mongo` for simpler command line invocation (e.g. `mvn mongo:start`)

## 0.1.13

* Add option to enable journaling

## 0.1.12

* Allow skipping with `-Dembedmongo.skip` 

## 0.1.11

* Update to [embedmongo.flapdoodle.de](https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de) 1.46.0 for performance improvements and latest versions.

## 0.1.10

* Update to [embedmongo.flapdoodle.de](https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de) 1.40 (thanks @cortiz)
* Add support for MongoDB authorization (thanks @chapmbk)
* Add `downloadPath` configuration option (thanks @dietrichatadobe)

## 0.1.8, 0.1.9

* Add `randomPort` configuration option (thanks @jumarko)

## 0.1.7

* Add `logFile`/`logFileEncoding` configuration options (thanks @matthewadams)
* Update to [embedmongo.flapdoodle.de](https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de) 1.31

## 0.1.6

* Add `proxyUser`/`proxyPassword` configuration options

## 0.1.5

* Update default mongo version to latest stable (2.2.1)
* Update to [embedmongo.flapdoodle.de](https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de) 1.27 (mkdir -p for databaseDirectory)

## 0.1.4

* Add `bindIp` configuration option (thanks @GTExcalibur)
* Update to [embedmongo.flapdoodle.de](https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de) 1.23 for explicit 2.0.7/2.2.0 support (thanks @davidmc24)

## 0.1.3

* Add support for any/all MongoDB versions, including new releases
* Update default mongo version from 2.1.1 (unstable) to 2.0.6 (stable)
* Add `logging` configuration with support for `console`, `file` and `none` mode

## 0.1.2

* Add `wait` configuration options to `start` goal to block immediately and wait until MongoDB is explicitly stopped (thanks @jeremynorris)
* Update to [embedmongo.flapdoodle.de](https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de) 1.16

## 0.1.1

* Add `proxyHost`/`proxyPort` configuration options for downloads via a proxy

# 0.1.0

* Add `port`, `version` and `databaseDirectory` configuration options
* Add `start` and `stop` goals
