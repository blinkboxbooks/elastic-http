package com.blinkbox.books.elasticsearch.client

case class GetResponse[T](
    found: Boolean,
    _index: String,
    _type: String,
    _id: String,
    _version: Option[Long],
    _source: Option[T])

case class IndexResponse(_index: String, _type: String, _id: String, _version: Long, created: Boolean)
