package com.blinkbox.books.elasticsearch.client

import com.sksamuel.elastic4s.DeleteIndexDefinition
import com.sksamuel.elastic4s.{ IndexDefinition, GetDefinition }
import com.sksamuel.elastic4s.ElasticDsl.{CreateIndexDefinition, SearchDefinition}
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.json4s.JValue
import spray.http.HttpEntity
import spray.http.MediaRanges
import spray.http.{ Uri, HttpRequest }
import spray.client.pipelining._
import spray.httpx.PipelineException
import spray.httpx.unmarshalling.FromResponseUnmarshaller
import spray.httpx.unmarshalling.Unmarshaller

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
      "replication_type" -> request.replicationType.toString,
      "consistency_level" -> request.consistencyLevel.toString,
      "op_type" -> request.opType.toString,
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

trait CreateIndexSupport {
  implicit object CreateIndexElasticRequest extends ElasticRequest[CreateIndexDefinition, AcknowledgedResponse] {
    override def request(req: CreateIndexDefinition): HttpRequest = {
      val builtRequest = req.build
      Put(s"/${builtRequest.indices.mkString(",")}", req._source.string())
    }
  }
}

trait DeleteIndexSupport {
  implicit object DeleteIndexElasticRequest extends ElasticRequest[DeleteIndexDefinition, AcknowledgedResponse] {
    override def request(req: DeleteIndexDefinition): HttpRequest = {
      val builtRequest = req.build
      Delete(s"/${builtRequest.indices.mkString(",")}")
    }
  }
}

trait CheckExistenceSupport {
  case class CheckExistence(index: String, `type`: Option[String] = None)

  implicit val UnitUnmarshaller = Unmarshaller[Unit](MediaRanges.`*/*`) {
    case HttpEntity.Empty => ()
    case _ => throw new PipelineException("Expected no-content in the response")
  }

  implicit object CheckExistenceElasticRequest extends ElasticRequest[CheckExistence, Unit] {
    override def request(req: CheckExistence): HttpRequest = {
      Head(s"/${req.index}${req.`type`.fold("")(t => s"/${t}")}")
    }
  }
}

trait StatusSupport {
  case object StatusRequest

  implicit object StatusElasticRequest extends ElasticRequest[StatusRequest.type, StatusResponse] {
    override def request(req: StatusRequest.type): HttpRequest = Get("/")
  }
}

object SprayElasticClientRequests
  extends IndexSupport
  with SearchSupport
  with GetSupport
  with TypedGetSupport
  with StatusSupport
  with CreateIndexSupport
  with DeleteIndexSupport
  with CheckExistenceSupport
