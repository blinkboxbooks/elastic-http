package com.blinkbox.books.elasticsearch.client.api

import com.blinkbox.books.elasticsearch.client._
import com.sksamuel.elastic4s.{DeleteIndexDefinition, GetDefinition, IndexDefinition}
import com.sksamuel.elastic4s.ElasticDsl.{DeleteByIdDefinition, UpdateDefinition}
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.update.UpdateRequest
import org.json4s.Extraction
import org.json4s.Formats
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
      "ttl" -> Option(request.ttl).filter(_ > 0).map(_.toString).orNull,
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

trait UpdateSupport {
  class UpdateElasticRequest(implicit val formats: Formats) extends ElasticRequest[UpdateDefinition, UpdateResponse] {
    def urlFor(req: UpdateRequest) = s"/${req.index}/${req.`type`}/${req.id}/_update"

    def fieldsFor(request: UpdateRequest): Map[String, String] = Option(request.fields).
      fold(Map.empty[String, String])(f => Map("fields" -> f.mkString(",")))

    def paramsFor(request: UpdateRequest): Map[String, String] = (Map(
      "routing" -> request.routing,
      "timeout" -> request.timeout.toString,
      "replication" -> request.replicationType.toString.toLowerCase,
      "refresh" -> request.refresh.toString,
      "retry_on_conflict" -> request.retryOnConflict.toString,
      "version" -> request.version.toString,
      "version_type" -> request.versionType.toString.toLowerCase
    ) ++ fieldsFor(request)).filter(_._2 != null)

    override def request(req: UpdateDefinition): HttpRequest = {
      val builtRequest = req.build
      val uri = Uri(urlFor(builtRequest)).withQuery(paramsFor(builtRequest))

      import org.json4s.JsonDSL._
      import org.json4s.jackson.JsonMethods._
      import scala.collection.convert.WrapAsScala._

      val bodyJson =
        ("script" -> Option(builtRequest.script)) ~
        ("params" -> Option(builtRequest.scriptParams).map(_.toMap).map(Extraction.decompose)) ~
        ("doc" -> Option(builtRequest.doc).map(d => parse(d.source.toUtf8))) ~
        ("detect_noop" -> Option(builtRequest.detectNoop)) ~
        ("upsert" -> Option(builtRequest.upsertRequest).map(r => parse(r.source.toUtf8)))

      Post(uri, compact(render(bodyJson)))
    }
  }

  implicit def updateElasticRequestInstance(implicit formats: Formats): ElasticRequest[UpdateDefinition, UpdateResponse] = new UpdateElasticRequest
}
