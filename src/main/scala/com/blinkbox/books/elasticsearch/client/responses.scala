package com.blinkbox.books.elasticsearch.client

case class GetResponse[T](
    found: Boolean,
    _index: String,
    _type: String,
    _id: String,
    _version: Option[Long],
    _source: Option[T])

case class ShardsStats(total: Int, successful: Int, failed: Int)
case class SearchHit[T](_index: String, _type: String, _id: Int, _source: T)
case class SearchHits[T](total: Int, hits: Array[SearchHit[T]])
case class SearchResponse[T](
  _shards: ShardsStats,
  hits: SearchHits[T]
)

case class IndexResponse(_index: String, _type: String, _id: String, _version: Long, created: Boolean)
