package com.blinkbox.books.elasticsearch.client

import akka.actor.{ActorRefFactory, ActorSystem}
import com.sksamuel.elastic4s.source.StringDocumentSource
import com.sksamuel.elastic4s.{GetDefinition, IndexDefinition}
import org.json4s.DefaultFormats
import spray.client.pipelining._
import spray.http.HttpHeaders.Host
import spray.http.{HttpCredentials, HttpRequest}
import spray.httpx.Json4sJacksonSupport
import spray.httpx.unmarshalling.FromResponseUnmarshaller

import scala.concurrent.{ExecutionContext, Future}

trait ElasticRequest[T, Response] {
  def request(req: T): HttpRequest
}

trait ElasticClient {
  def execute[T, Response](request: T)(implicit er: ElasticRequest[T, Response]): Future[Response]
}

class SprayElasticClient(host: String, port: Int, useHttps: Boolean = false, credentials: Option[HttpCredentials] = None)(implicit arf: ActorRefFactory, ec: ExecutionContext) {

  val setHost = (req: HttpRequest) => req.withEffectiveUri(useHttps, Host(host, port))

  val setCredentials = credentials.fold[HttpRequest => HttpRequest](identity)(addCredentials)

  val basePipeline = setHost ~> setCredentials ~> sendReceive

  def pipeline[Out: FromResponseUnmarshaller] = basePipeline ~> unmarshal[Out]

  def execute[T, Response](request: T)(implicit er: ElasticRequest[T, Response], fru: FromResponseUnmarshaller[Response]): Future[Response] =
    er.request(request) ~> pipeline[Response]
}

object ElasticClient {
  implicit object GetElasticRequest extends ElasticRequest[GetDefinition, GetResponse] {
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


