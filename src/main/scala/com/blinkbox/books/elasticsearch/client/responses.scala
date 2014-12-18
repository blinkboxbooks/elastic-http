package com.blinkbox.books.elasticsearch.client

import spray.http.StatusCode

case class GetResponse[T](
  found: Boolean,
  _index: String,
  _type: String,
  _id: String,
  _version: Option[Long],
  _source: Option[T]
)

case class ShardsStats(total: Int, successful: Int, failed: Int)
case class SearchHit[T](_index: String, _type: String, _id: String, _source: T)
case class SearchHits[T](total: Int, hits: Seq[SearchHit[T]])
case class SearchResponse[T](
  _shards: ShardsStats,
  hits: SearchHits[T]
)

case class IndexResponse(_index: String, _type: String, _id: String, _version: Long, created: Boolean)

case class Version(
  number: String,
  build_hash: String,
  build_timestamp: String,
  build_snapshot: Boolean,
  lucene_version: String
)
case class StatusResponse(
  status: Int,
  name: String,
  cluster_name: String,
  tagline: String,
  version: Version
)

case class AcknowledgedResponse(acknowledged: Boolean)

case class DeleteResponse(found: Boolean, _index: String, _type: String, _id: String, _version: Long)

case class RefreshIndicesResponse(_shards: ShardsStats)

case class FailedRequest(statusCode: StatusCode) extends Exception(s"Error from ES: $statusCode")
