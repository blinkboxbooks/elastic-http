package com.blinkbox.books.elasticsearch.client

import com.sksamuel.elastic4s.{ IndexDefinition, GetDefinition }
import com.sksamuel.elastic4s.ElasticDsl.SearchDefinition
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.json4s.JValue
import spray.http.{ Uri, HttpRequest }
import spray.client.pipelining._
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
      "ttl" -> request.ttl.toString,
      "versionType" -> request.versionType.toString,
      "version" -> request.version.toString,
      "refresh" -> request.refresh.toString,
      "timeout" -> request.timeout.toString,
      "replicationType" -> request.replicationType.toString,
      "consistencyLevel" -> request.consistencyLevel.toString,
      "opType" -> request.opType.toString,
      "timestamp" -> request.timestamp,
      "parent" -> request.parent,
      "routing" -> request.routing
    )

    override def request(req: IndexDefinition): HttpRequest = {
      val builtRequest = req.build
      val uri = Uri(urlFor(builtRequest)).withQuery(paramsFor(builtRequest))

      Put(uri, req._fieldsAsXContent.string)
    }
  }
}

trait SearchSupport {
  sealed trait SearchBase {
    def urlFor(req: SearchRequest) = s"/${req.indices.mkString(",")}/${req.types.mkString(",")}/_search"

    def paramsFor(request: SearchRequest): Map[String, String] =
      Option(request.routing).fold(Map.empty[String, String])(r => Map("routing" -> r))

    def requestFor(req: SearchDefinition): HttpRequest = {
      val builtRequest = req.build
      val uri = Uri(urlFor(builtRequest)).withQuery(paramsFor(builtRequest))

      Post(uri, req._builder.toString)
    }
  }

  implicit object SearchElasticRequest extends ElasticRequest[SearchDefinition, SearchResponse[JValue]] with SearchBase {
    override def request(req: SearchDefinition): HttpRequest = requestFor(req)
  }
}

trait TypedSearchSupport {
  this: SearchSupport =>

  case class TypedSearchDefinition[T: FromResponseUnmarshaller](req: SearchDefinition)

  implicit class SearchDefinitionOps(val req: SearchDefinition) {
    def sourceIs[T: FromResponseUnmarshaller] = TypedSearchDefinition[T](req)
  }

  class TypedSearchElasticRequest[T: FromResponseUnmarshaller]
    extends ElasticRequest[SearchDefinition, SearchResponse[T]]
    with SearchBase {

    override def request(req: SearchDefinition): HttpRequest = requestFor(req.req)
  }

  implicit def typedSearchElasticRequest[T: FromResponseUnmarshaller] = new TypedSearchElasticRequest[T]
}

object SprayElasticClientRequests
  extends IndexSupport
  with SearchSupport
  with GetSupport
  with TypedGetSupport
