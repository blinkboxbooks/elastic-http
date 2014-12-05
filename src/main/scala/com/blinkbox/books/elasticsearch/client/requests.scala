package com.blinkbox.books.elasticsearch.client

import com.sksamuel.elastic4s.{IndexDefinition, GetDefinition}
import org.json4s.JValue
import spray.http.HttpRequest
import spray.client.pipelining._
import spray.httpx.unmarshalling.FromResponseUnmarshaller

trait ClientRequests {
  implicit object GetElasticRequest extends ElasticRequest[GetDefinition, GetResponse[JValue]] {
    override def request(req: GetDefinition): HttpRequest = {
      val builtRequest = req.build
      Get(s"/${builtRequest.index}/${builtRequest.`type`}/${builtRequest.id}")
    }
  }

  implicit object IndexElasticRequest extends ElasticRequest[IndexDefinition, IndexResponse] {
    override def request(req: IndexDefinition): HttpRequest = {
      val builtRequest = req.build
      Put(s"/${builtRequest.index}/${builtRequest.`type`}/${builtRequest.id}", req._fieldsAsXContent.string)
    }
  }
}

trait TypedClientRequests {
  case class TypedGetDefinition[T: FromResponseUnmarshaller](req: GetDefinition)

  implicit class TypedGetDefinitionOps(req: GetDefinition) {
    def sourceIs[T: FromResponseUnmarshaller] = TypedGetDefinition[T](req)
  }

  class TypedGetElasticRequest[T: FromResponseUnmarshaller] extends ElasticRequest[TypedGetDefinition[T], GetResponse[T]] {
    override def request(typedReq: TypedGetDefinition[T]): HttpRequest = {
      val builtRequest = typedReq.req.build
      Get(s"/${builtRequest.index}/${builtRequest.`type`}/${builtRequest.id}")
    }
  }

  import scala.language.implicitConversions

  implicit def toTypedGetElasticRequest[T](implicit marshaller: FromResponseUnmarshaller[T]): TypedGetElasticRequest[T] = new TypedGetElasticRequest[T]
}

object SprayElasticClientRequests
  extends ClientRequests
  with TypedClientRequests
