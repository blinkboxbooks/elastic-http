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

case class MultiGetResponse[T](docs: Seq[GetResponse[T]])

case class ShardsStats(total: Int, successful: Int, failed: Int)
case class SearchHit[T](_index: String, _type: String, _id: String, _source: T)
case class SearchHits[T](total: Int, hits: Seq[SearchHit[T]])
case class SuggestionOption[Payload](text: String, score: Double, collate_match: Option[Boolean], payload: Option[Payload])
case class Suggestion[Payload](text: String, offset: Int, length: Int, options: Seq[SuggestionOption[Payload]])
case class SearchResponse[Document, Payload](
  _shards: ShardsStats,
  hits: SearchHits[Document],
  suggest: Option[Map[String, Seq[Suggestion[Payload]]]]
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

sealed trait FailedRequest
case class UnsuccessfulResponse(statusCode: StatusCode, content: String)
  extends RuntimeException(s"Error from ES: $statusCode") with FailedRequest
case class RequestException(message: String, cause: Throwable)
  extends RuntimeException(message, cause) with FailedRequest

case class UpdateResponse(_index: String, _type: String, _id: String, _version: Long)

sealed trait BulkResponseItem {
  def _index: String
  def _type: String
  def _id: String
  def status: StatusCode
  def error: Option[String]
}
case class DeleteResponseItem(
  _index: String,
  _type: String,
  _id: String,
  _version: Long,
  status: StatusCode,
  found: Boolean,
  error: Option[String]
) extends BulkResponseItem
case class UpdateResponseItem(
  _index: String,
  _type: String,
  _id: String,
  status: StatusCode,
  error: Option[String]
) extends BulkResponseItem
case class IndexResponseItem(
  _index: String,
  _type: String,
  _id: String,
  _version: Long,
  status: StatusCode,
  error: Option[String]
) extends BulkResponseItem
case class BulkResponse(took: Int, errors: Boolean, items: Seq[BulkResponseItem])
