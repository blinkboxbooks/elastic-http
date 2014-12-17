package com.blinkbox.books.elasticsearch.client

import akka.actor.ActorSystem
import org.json4s.DefaultFormats
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Suite
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Millis
import org.scalatest.time.Span
import spray.httpx.Json4sJacksonSupport
import spray.httpx.unmarshalling.FromResponseUnmarshaller

trait ElasticTest extends ScalaFutures with BeforeAndAfterAll {
  this: Suite =>

  object JsonSupport extends Json4sJacksonSupport {
    implicit val json4sJacksonFormats = DefaultFormats
  }

  override implicit def patienceConfig = PatienceConfig(timeout = Span(3000, Millis), interval = Span(100, Millis))

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

  case class RequestDone[Resp](check: (Resp => Unit) => Unit)

  def whenRequestDone[Req, Resp](req: Req)(implicit esr: ElasticRequest[Req, Resp], fru: FromResponseUnmarshaller[Resp]): RequestDone[Resp] = {
    val resp = client.execute(req)
    RequestDone(whenReady(resp)(_: Resp => Unit))
  }

}
