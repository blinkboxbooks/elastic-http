package com.blinkbox.books.elasticsearch.client

import akka.actor.ActorSystem
import com.blinkbox.books.test.FailHelper
import com.sksamuel.elastic4s.ElasticDsl
import org.json4s.DefaultFormats
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Suite
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Millis
import org.scalatest.time.Span
import spray.httpx.Json4sJacksonSupport
import spray.httpx.unmarshalling.FromResponseUnmarshaller

trait ElasticTest extends ScalaFutures with FailHelper with BeforeAndAfterAll with BeforeAndAfterEach with DeleteIndexSupport {
  this: Suite =>

  override implicit def patienceConfig = PatienceConfig(timeout = Span(3000, Millis), interval = Span(100, Millis))

  object JsonSupport extends Json4sJacksonSupport {
    implicit val json4sJacksonFormats = DefaultFormats
  }

  import JsonSupport.json4sUnmarshaller

  implicit val as = ActorSystem("examples")
  implicit val ec = as.dispatcher

  val client = new SprayElasticClient("localhost", 12345)

  var es: EmbeddedElasticSearch = _

  override def beforeAll() {
    super.beforeAll()
    es = new EmbeddedElasticSearch
    es.start()
  }

  override def afterAll() {
    es.stop()
  }

  override def beforeEach() {
    import ElasticDsl._
    client.execute(delete index "_all")
  }

  def isOk[T] = { _: T => () }

  case class SuccessfulRequest[Resp](check: (Resp => Unit) => Unit)

  def successfulRequest[Req, Resp](req: Req)(implicit
      esr: ElasticRequest[Req, Resp],
      fru: FromResponseUnmarshaller[Resp]): SuccessfulRequest[Resp] = {
    val resp = client.execute(req)
    SuccessfulRequest(whenReady(resp)(_: Resp => Unit))
  }

  def failingRequest[Req](req: Req)(implicit esr: ElasticRequest[Req, _]) = {
    failingWith[FailedRequest](client.execute(req))
  }

}
