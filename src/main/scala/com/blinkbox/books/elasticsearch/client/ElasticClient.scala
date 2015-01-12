package com.blinkbox.books.elasticsearch.client

import akka.actor.ActorRefFactory
import com.blinkbox.books.elasticsearch.client.api._
import spray.client.pipelining._
import spray.http.HttpHeaders.Host
import spray.http.{HttpCredentials, HttpRequest, HttpResponse}
import spray.httpx.UnsuccessfulResponseException
import spray.httpx.unmarshalling.FromResponseUnmarshaller

import scala.concurrent.{ExecutionContext, Future}

trait ElasticRequest[T, Response] {
  def request(req: T): HttpRequest
}

trait ElasticClient {
  def execute[T, Response](request: T)(implicit
      er: ElasticRequest[T, Response],
      fru: FromResponseUnmarshaller[Response]): Future[Response]
}

class SprayElasticClient(
  host: String,
  port: Int,
  useHttps: Boolean = false,
  credentials: Option[HttpCredentials] = None,
  requestLogFn: HttpRequest => Unit = _ => (),
  responseLogFn: HttpResponse => Unit = _ => ())(implicit arf: ActorRefFactory, ec: ExecutionContext) extends ElasticClient {

  private val setHost = (req: HttpRequest) => req.withEffectiveUri(useHttps, Host(host, port))

  private val setCredentials = credentials.fold[HttpRequest => HttpRequest](identity)(addCredentials)

  private val logRequest = { (req: HttpRequest) =>
    requestLogFn(req)
    req
  }

  private val logResponse = { (resp: HttpResponse) =>
    responseLogFn(resp)
    resp
  }

  private val basePipeline = setHost ~> setCredentials ~> logRequest ~> sendReceive ~> logResponse

  private def pipeline[Out: FromResponseUnmarshaller] = basePipeline ~> unmarshal[Out]

  def execute[T, Response](request: T)(implicit
      er: ElasticRequest[T, Response],
      fru: FromResponseUnmarshaller[Response]): Future[Response] =
    (er.request(request) ~> pipeline[Response]) transform(identity, {
      case ex: UnsuccessfulResponseException =>
        UnsuccessfulResponse(ex.response.status, ex.response.entity.asString)
      case ex =>
        RequestException(ex.getMessage, ex)
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
  with UpdateSupport
  with BulkSupport
