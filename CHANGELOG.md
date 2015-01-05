# Change log

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

