package com.blinkbox.books.elasticsearch.client.api

import com.blinkbox.books.elasticsearch.client._
import com.sksamuel.elastic4s.{DeleteIndexDefinition, GetDefinition, IndexDefinition}
import com.sksamuel.elastic4s.ElasticDsl.DeleteByIdDefinition
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.json4s.JValue
import spray.client.pipelining._
import spray.http.{HttpRequest, Uri}
import spray.httpx.unmarshalling.FromResponseUnmarshaller

trait GetSupport {
  sealed trait GetBase {
    def urlFor(request: GetRequest) = s"/${request.index}/${request.`type`}/${request.id}"

    def fieldsFor(request: GetRequest): Map[String, String] = Option(request.fields).
      fold(Map.empty[String, String])(f => Map("fields" -> f.mkString(",")))

    def paramsFor(request: GetRequest): Map[String, String] = Map(
      "realtime" -> request.realtime.toString,
      "ignoreErrorsOnGeneratedFields" -> request.ignoreErrorsOnGeneratedFields.toString,
      "routing" -> request.routing,
      "preference" -> request.preference,
      "refresh" -> request.refresh.toString,
      "version" -> request.version.toString,
      "versionType" -> request.versionType.toString
    ) ++ fieldsFor(request)

    def requestFor(req: GetDefinition) = {
      val builtRequest = req.build
      Get(s"/${builtRequest.index}/${builtRequest.`type`}/${builtRequest.id}")
    }
  }

  implicit object GetElasticRequest extends ElasticRequest[GetDefinition, GetResponse[JValue]] with GetBase {
    override def request(req: GetDefinition): HttpRequest = requestFor(req)
  }
}

trait TypedGetSupport {
  this: GetSupport =>

  case class TypedGetDefinition[T: FromResponseUnmarshaller](req: GetDefinition)

  implicit class TypedGetDefinitionOps(req: GetDefinition) {
    def sourceIs[T: FromResponseUnmarshaller] = TypedGetDefinition[T](req)
  }

  class TypedGetElasticRequest[T: FromResponseUnmarshaller]
    extends ElasticRequest[TypedGetDefinition[T], GetResponse[T]] with GetBase {

    override def request(typedReq: TypedGetDefinition[T]): HttpRequest = requestFor(typedReq.req)
  }

  import scala.language.implicitConversions

  implicit def toTypedGetElasticRequest[T: FromResponseUnmarshaller]: TypedGetElasticRequest[T] =
    new TypedGetElasticRequest[T]
}

trait IndexSupport {
  implicit object IndexElasticRequest extends ElasticRequest[IndexDefinition, IndexResponse] {
    def maybeSegment(param: String) = Option(param).fold("")(p => s"/$p")

    def urlFor(req: IndexRequest) = s"/${req.index}/${req.`type`}${maybeSegment(req.id)}"

    def paramsFor(request: IndexRequest): Map[String, String] = Map(
      "ttl" -> Option(request.ttl).filter(_ > 0).map(_.toString).getOrElse(null),
      "version_type" -> request.versionType.name.toLowerCase,
      "version" -> request.version.toString,
      "refresh" -> request.refresh.toString,
      "timeout" -> request.timeout.toString,
      "replication" -> request.replicationType.toString.toLowerCase,
      "consistency" -> request.consistencyLevel.toString.toLowerCase,
      "op_type" -> request.opType.toString.toLowerCase,
      "timestamp" -> request.timestamp,
      "parent" -> request.parent,
      "routing" -> request.routing
    ).filter(_._2 != null)

    override def request(req: IndexDefinition): HttpRequest = {
      val builtRequest = req.build
      val uri = Uri(urlFor(builtRequest)).withQuery(paramsFor(builtRequest))
      val source = builtRequest.source.toUtf8

      Option(builtRequest.id).fold(Post)(_ => Put)(uri, builtRequest.source.toUtf8)
    }
  }
}

trait DeleteByIdSupport {
  implicit object DeleteByIdElasticRequest extends ElasticRequest[DeleteByIdDefinition, DeleteResponse] {

    def urlFor(req: DeleteRequest) = s"/${req.index}/${req.`type`}/${req.id}"

    def paramsFor(request: DeleteRequest): Map[String, String] = Map(
      "version_type" -> request.versionType.name.toLowerCase,
      "version" -> request.version.toString,
      "refresh" -> request.refresh.toString,
      "timeout" -> request.timeout.toString,
      "replication" -> request.replicationType.toString.toLowerCase,
      "consistency" -> request.consistencyLevel.toString.toLowerCase,
      "routing" -> request.routing
    ).filter(_._2 != null)

    override def request(req: DeleteByIdDefinition): HttpRequest = {
      val builtRequest = req.build

      Delete(Uri(urlFor(builtRequest)).withQuery(paramsFor(builtRequest)))
    }
  }
}
