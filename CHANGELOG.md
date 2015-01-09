# Change log

## 0.0.10 ([#12](https://git.mobcastdev.com/Labs/elastic-http/pull/12) 2015-01-09 15:57:33)

Make a distinction between different types of ES request failures

Patch

- create two different types of exceptions: for when it's an expected failure, and when there is an internal/connection exception (i.e. ConnectionAttemptFailedException)


## 0.0.9 ([#11](https://git.mobcastdev.com/Labs/elastic-http/pull/11) 2015-01-09 12:03:12)

Remove the JsonSupport and publish individual formats

### Improvements

Remove the public `JsonSupport` trait and publish the individual formats to be used by the library clients.

## 0.0.8 ([#10](https://git.mobcastdev.com/Labs/elastic-http/pull/10) 2015-01-09 11:19:33)

Introduce logging capabilities for HTTP request/response

### Improvements

This patch allows passing two functions to the `SprayElasticClient` in order to log what is being sent to and what is being received from ES.

## 0.0.7 ([#9](https://git.mobcastdev.com/Labs/elastic-http/pull/9) 2015-01-08 14:09:54)

PT-571 Introduce (typed) suggestions support for search

### Improvements

Introduce spellcheck and completion suggestions support with optional precise typing.

## 0.0.6 ([#8](https://git.mobcastdev.com/Labs/elastic-http/pull/8) 2015-01-06 10:08:06)

Make common bulk response fields as part of the BulkResponseItem trait.

Patch

## 0.0.5 ([#5](https://git.mobcastdev.com/Labs/elastic-http/pull/5) 2015-01-05 16:56:24)

Add query parameters to GET document URL

### Bugfix

Fix missing parameters on the URL for document GET API

## 0.0.4 ([#7](https://git.mobcastdev.com/Labs/elastic-http/pull/7) 2015-01-05 11:35:44)

Bulk support

### Improvements

This patch introduces support for [Elasticsearch bulk APIs](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/docs-bulk.html) and Update APIs.


## 0.0.3 ([#4](https://git.mobcastdev.com/Labs/elastic-http/pull/4) 2014-12-23 12:15:25)

Attempt to reduce the footprint of the embedded ES

### Improvements

Some attempts to make tests more stable when using the embedded ES server:

* Do not shard data
* Do not replicate data
* Do not attempt multicast discovery of other nodes
* Store index in-memory

## 0.0.2 ([#1](https://git.mobcastdev.com/Labs/elastic-http/pull/1) 2014-12-23 10:14:08)

A bit of cleanup; introduce VERSION.

Patch

- a bit of cleanup;
- introduce the VERSION file.

## 0.0.1 ([#3](https://git.mobcastdev.com/Labs/elastic-http/pull/3) 2014-12-23 09:58:27)

Increase timeout for test start-up

### Improvements

