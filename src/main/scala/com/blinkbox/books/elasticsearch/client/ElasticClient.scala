package com.blinkbox.books.elasticsearch.client

import akka.actor.ActorRefFactory
import spray.client.pipelining._
import spray.http.HttpHeaders.Host
import spray.http.{HttpCredentials, HttpRequest}
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
