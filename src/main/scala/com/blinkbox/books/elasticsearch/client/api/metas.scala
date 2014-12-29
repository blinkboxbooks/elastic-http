package com.blinkbox.books.elasticsearch.client.api

import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.update.UpdateRequest

case class IndexMeta(
  _index: String,
  _type: String,
  _id: String,
  ttl: Option[Long],
  version_type: String,
  version: Long,
  refresh: Boolean,
  timeout: String,
  replication: String,
  consistency: String,
  op_type: String,
  timestamp: String,
  parent: String,
  routing: String
) {
  def toQueryMap: Map[String, String] = Map(
    "ttl" -> ttl.map(_.toString).orNull,
    "version_type" -> version_type,
    "version" -> version.toString,
    "refresh" -> refresh.toString,
    "timeout" -> timeout,
    "replication" -> replication,
    "consistency" -> consistency,
    "op_type" -> op_type,
    "timestamp" -> timestamp,
    "parent" -> parent,
    "routing" -> routing
  ).filter(_._2 != null)
}

object IndexMeta {
  def fromRequest(req: IndexRequest): IndexMeta = IndexMeta(
    _index = req.index,
    _type = req.`type`,
    _id = req.id,
    ttl = Option(req.ttl).filter(_ > 0),
    version_type = req.versionType.toString.toLowerCase,
    version = req.version,
    refresh = req.refresh,
    timeout = req.timeout.toString,
    replication = req.replicationType.toString.toLowerCase,
    consistency = req.consistencyLevel.toString.toLowerCase,
    op_type = req.opType.toString.toLowerCase,
    timestamp = req.timestamp,
    parent = req.parent,
    routing = req.routing
  )
}

case class UpdateMeta(
  _index: String,
  _type: String,
  _id: String,
  routing: String,
  timeout: String,
  replication: String,
  refresh: Boolean,
  retry_on_conflict: Int,
  version: Long,
  version_type: String,
  fields: Seq[String]
) {
  def toQueryMap: Map[String, String] = (Map(
    "routing" -> routing,
    "timeout" -> timeout,
    "replication" -> replication,
    "refresh" -> refresh.toString,
    "retry_on_conflict" -> retry_on_conflict.toString,
    "version" -> version.toString,
    "version_type" -> version_type,
    "fields" -> Option(fields).map(_.mkString(",")).orNull
  )).filter(_._2 != null)
}

object UpdateMeta {
  def fromRequest(req: UpdateRequest): UpdateMeta = UpdateMeta(
    _index = req.index,
    _type = req.`type`,
    _id = req.id,
    routing = req.routing,
    timeout = req.timeout.toString,
    replication = req.replicationType.toString.toLowerCase,
    refresh = req.refresh,
    retry_on_conflict = req.retryOnConflict,
    version = req.version,
    version_type = req.versionType.toString.toLowerCase,
    fields = Option(req.fields).map(_.toSeq).orNull
  )
}

case class DeleteMeta(
  _index: String,
  _type: String,
  _id: String,
  version_type: String,
  version: Long,
  refresh: Boolean,
  timeout: String,
  replication: String,
  consistency: String,
  routing: String
) {
  def toQueryMap: Map[String, String] = Map(
  "version_type" -> version_type,
  "version" -> version.toString,
  "refresh" -> refresh.toString,
  "timeout" -> timeout,
  "replication" -> replication,
  "consistency" -> consistency,
  "routing" -> routing
  ).filter(_._2 != null)
}

object DeleteMeta {
  def fromRequest(req: DeleteRequest): DeleteMeta = DeleteMeta(
    _index = req.index,
    _type = req.`type`,
    _id = req.id,
    version_type = req.versionType.name.toLowerCase,
    version = req.version,
    refresh = req.refresh,
    timeout = req.timeout.toString,
    replication = req.replicationType.toString.toLowerCase,
    consistency = req.consistencyLevel.toString.toLowerCase,
    routing = req.routing
  )
}
