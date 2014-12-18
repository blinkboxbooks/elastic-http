package com.blinkbox.books.elasticsearch.client

import akka.actor.ActorRefFactory
import scala.concurrent.{ExecutionContext, Future}
import spray.client.pipelining._
import spray.http.{HttpCredentials, HttpRequest}
import spray.http.HttpHeaders
import spray.http.HttpHeaders.Host
import spray.httpx.UnsuccessfulResponseException
import spray.httpx.unmarshalling.FromResponseUnmarshaller

import api._

trait ElasticRequest[T, Response] {
  def request(req: T): HttpRequest
}

trait ElasticClient {
  def execute[T, Response](request: T)(implicit
      er: ElasticRequest[T, Response],
      fru: FromResponseUnmarshaller[Response]): Future[Response]
}

class SprayElasticClient(host: String, port: Int, useHttps: Boolean = false, credentials: Option[HttpCredentials] = None)(implicit arf: ActorRefFactory, ec: ExecutionContext) extends ElasticClient {

  val setHost = (req: HttpRequest) => req.withEffectiveUri(useHttps, Host(host, port))

  val setCredentials = credentials.fold[HttpRequest => HttpRequest](identity)(addCredentials)

  val basePipeline = setHost ~> setCredentials ~> sendReceive

  def pipeline[Out: FromResponseUnmarshaller] = basePipeline ~> unmarshal[Out]

  def execute[T, Response](request: T)(implicit
      er: ElasticRequest[T, Response],
      fru: FromResponseUnmarshaller[Response]): Future[Response] =
    (er.request(request) ~> pipeline[Response]) transform(identity, {
      case ex: UnsuccessfulResponseException => FailedRequest(ex.response.status)
    })
}

object ElasticClientApi
  extends IndexSupport
  with SearchSupport
  with TypedSearchSupport
  with GetSupport
  with TypedGetSupport
  with StatusSupport
  with CreateIndexSupport
  with DeleteIndexSupport
  with CheckExistenceSupport
  with DeleteByIdSupport
  with RefreshIndicesSupport
