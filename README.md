# Elasticsearch HTTP Client [![Build Status](https://travis-ci.org/blinkboxbooks/elastic-http.svg?branch=master)](https://travis-ci.org/blinkboxbooks/elastic-http)

This project is an adapter for the [Elastic4s library](https://github.com/sksamuel/elastic4s) to communicate with ElasticSearch over HTTP.

## Rationale

The reason why we started this project is to explore options to allow using the HTTP protocol to communicate with an ES cluster from our scala projects. We started our development using [Elastic4s](https://github.com/sksamuel/elastic4s) as it provides a neat DSL for writing queries in a way that is very close to the JSON DSL provided by ES itself. The official Java API provided by the ES development team and most libraries out there (including the aforementioned Elastic4s) use a binary protocol over TCP to communicate with an ES cluster; in our case we would prefer using the HTTP protocol as it has been asked by our DevOps team for the following reasons:

* No [JVM-version dependencies](http://www.elasticsearch.org/guide/en/elasticsearch/guide/current/_java_virtual_machine.html)
* Easier to set-up authentication: as ES doesn't provide an authentication module at the moment it is easier to provide authentication over HTTP proxies
* Control over load-balancing between instances as in the binary protocol the load balancing is done by the cluster itself

## About this project

This is an attempt to make it easier for us to continue developing our services with the tools we chose and with minimum disruption for the existing code. For examples of usage please go to the [tests folder](https://git.mobcastdev.com/Labs/elastic-http/tree/master/src/test/scala/com/blinkbox/books/elasticsearch/client); tests follow the same categorization used on the ES API guide.

### Installation

The project is available on Maven Central, you can depend on it by adding the following to your `build.sbt`:

```
libraryDependencies += "com.blinkbox.books" %% "elastic-http" % "0.0.12"
```

This project requires *Scala >= 2.11.5*.

### ElasticClient

The `ElasticClient` interface provided mimics the Elastic4S client class by providing the same `execute` function but using a better overloading technique based on type-classes to construct the actual request to be performed by the underlying Spray client. Support for more APIs can be added incrementally and missing support will give raise to compilation errors.

### ElasticRequest[Req, Resp]

For each request to be supported by the client an instance of the `ElasticClient` class needs to be implicitly available in scope; this instance will also decide what the return type of the `execute` function will be and allows for extending the DSL to provide *typed requests* as well (i.e. requests in which the response document is de-serialized to a specified type).

The request instances implemented are divided in files following the ES guide's classification; specifically the following ones are provided at the moment:

* Index APIs:
    * Create index
    * Delete index
    * Refresh index (i.e. make pending changes available for search)
    * Check index/type existence
    * Get index status

* Document APIs:
    * Index a document (create/update)
    * Get a document by id (with optional type support)
    * Delete a document by id
    * Update
    * Bulk index/update/delete operations
    * Multi-get (with optional type support)

* Search APIs:
    * Perform a search query (with optional type support)

### Testing

The implemented functionalities are tested using an embedded ES instance which opens on a random port in the range `[12000; 12100[`

Adding more tests is trivial by extending the `ElasticTest` trait and following the existing ones.
