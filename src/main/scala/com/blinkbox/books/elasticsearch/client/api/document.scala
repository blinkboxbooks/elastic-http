package com.blinkbox.books.elasticsearch.client.api

import com.blinkbox.books.elasticsearch.client._
import com.sksamuel.elastic4s.{BulkDefinition, DeleteIndexDefinition, GetDefinition, IndexDefinition}
import com.sksamuel.elastic4s.ElasticDsl.{DeleteByIdDefinition, UpdateDefinition}
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.index.VersionType
import org.json4s.{Extraction, Formats, JValue}
import spray.client.pipelining._
import spray.http.{HttpRequest, Uri}
import spray.httpx.unmarshalling.FromResponseUnmarshaller

trait GetSupport {
  sealed trait GetBase {
    def urlFor(request: GetRequest) = Uri(s"/${request.index}/${request.`type`}/${request.id}").withQuery(paramsFor(request))

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

    def paramsFor(request: IndexRequest): Map[String, String] = IndexMeta.fromRequest(request).toQueryMap

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

    override def request(req: DeleteByIdDefinition): HttpRequest = {
      val builtRequest = req.build

      Delete(Uri(urlFor(builtRequest)).withQuery(DeleteMeta.fromRequest(builtRequest).toQueryMap))
    }
  }
}

object UpdateSupport {
  def requestBody(req: UpdateRequest)(implicit formats: Formats) = {
    import org.json4s.JsonDSL._
    import org.json4s.jackson.JsonMethods._
    import scala.collection.convert.WrapAsScala._

    ("script" -> Option(req.script)) ~
      ("scripted_upsert" -> req.scriptedUpsert) ~
      ("doc_as_upsert" -> req.docAsUpsert) ~
      ("upsert" -> Option(req.upsertRequest).map(_.source.toUtf8).map(parse(_))) ~
      ("doc" -> Option(req.doc).map(_.source.toUtf8).map(parse(_))) ~
      ("params" -> Option(req.scriptParams).map(_.toMap).map(Extraction.decompose)) ~
      ("detect_noop" -> Option(req.detectNoop))
  }

}
trait UpdateSupport {
  class UpdateElasticRequest(implicit val formats: Formats) extends ElasticRequest[UpdateDefinition, UpdateResponse] {
    def urlFor(req: UpdateRequest) = s"/${req.index}/${req.`type`}/${req.id}/_update"

    override def request(req: UpdateDefinition): HttpRequest = {
      val builtRequest = req.build
      val meta = UpdateMeta.fromRequest(builtRequest)
      val uri = Uri(urlFor(builtRequest)).withQuery(meta.toQueryMap)

      import org.json4s.jackson.JsonMethods._

      Post(uri, compact(render(UpdateSupport.requestBody(builtRequest))))
    }
  }

  implicit def updateElasticRequestInstance(implicit formats: Formats): ElasticRequest[UpdateDefinition, UpdateResponse] =
    new UpdateElasticRequest
}

trait BulkSupport {
  import org.json4s.jackson.JsonMethods._
  import org.json4s.jackson.Serialization

  class BulkElasticRequest(implicit format: Formats) extends ElasticRequest[BulkDefinition, BulkResponse] {
    def indexAction(req: IndexRequest) = Map("index" -> IndexMeta.fromRequest(req))
    def updateAction(req: UpdateRequest) = Map("update" -> UpdateMeta.fromRequest(req))
    def deleteAction(req: DeleteRequest) = Map("delete" -> DeleteMeta.fromRequest(req))

    override def request(req: BulkDefinition): HttpRequest = {
      import scala.collection.convert.WrapAsScala._

      val reqBodies: Iterable[String] = req._builder.requests.flatMap {
        case idx: IndexRequest =>
          Serialization.write(indexAction(idx)) :: idx.source.toUtf8 :: Nil
        case upd: UpdateRequest =>
          Serialization.write(updateAction(upd)) :: compact(render(UpdateSupport.requestBody(upd))) :: Nil
        case del: DeleteRequest =>
          Serialization.write(deleteAction(del)) :: Nil
      }

      Post("/_bulk", reqBodies.mkString("\n") + "\n")
    }
  }

  implicit def bulkElasticRequestInstance(implicit formats: Formats): ElasticRequest[BulkDefinition, BulkResponse] =
    new BulkElasticRequest
}
