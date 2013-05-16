# embedmongo-maven-plugin Changelog

## 0.1.8

* Add support for random port allocation - new configuration option `randomPort`

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
