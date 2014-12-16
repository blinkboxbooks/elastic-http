package com.blinkbox.books.elasticsearch.client

import akka.actor.ActorSystem
import org.json4s.DefaultFormats
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Millis
import org.scalatest.time.Span
import org.scalatest.{FlatSpec, Matchers}
import spray.httpx.Json4sJacksonSupport
import spray.httpx.unmarshalling.FromResponseUnmarshaller

class IndexAndGetSpecs extends FlatSpec with Matchers with BeforeAndAfterAll with ScalaFutures {

  object JsonSupport extends Json4sJacksonSupport {
    implicit val json4sJacksonFormats = DefaultFormats
  }

  override implicit def patienceConfig = PatienceConfig(timeout = Span(3000, Millis), interval = Span(100, Millis))

  implicit val as = ActorSystem("examples")
  implicit val ec = as.dispatcher

  val client = new SprayElasticClient("localhost", 9200)

  import com.blinkbox.books.elasticsearch.client.SprayElasticClientRequests._
  import com.sksamuel.elastic4s.ElasticDsl._
  import JsonSupport.json4sUnmarshaller

  var es: EmbeddedElasticSearch = _

  override def beforeAll() {
    super.beforeAll()
    es = new EmbeddedElasticSearch
  }

  override def afterAll() {
    es.stop()
  }

  case class RequestDone[Resp](check: (Resp => Unit) => Unit)

  def whenRequestDone[Req, Resp](req: Req)(implicit esr: ElasticRequest[Req, Resp], fru: FromResponseUnmarshaller[Resp]): RequestDone[Resp] = {
    val resp = client.execute(req)
    RequestDone(whenReady(resp)(_: Resp => Unit))
  }

  "The ES client" should "be able to ping an ES instance" in {
    whenRequestDone(StatusRequest) check { resp =>
      resp.status should equal(200)
    }
  }

  it should "be able to create an index with some mappings" in {
    // TODO...
  }
}
