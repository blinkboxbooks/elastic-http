package com.blinkbox.books.elasticsearch.client

import akka.actor.ActorSystem
import java.util.concurrent.atomic.AtomicReference
import org.scalatest.{Assertions, BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import org.scalatest.concurrent.{AsyncAssertions, PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Millis, Span}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import spray.httpx.Json4sJacksonSupport
import spray.httpx.unmarshalling.FromResponseUnmarshaller

trait ElasticTest extends Assertions with AsyncAssertions with ScalaFutures with BeforeAndAfterAll with BeforeAndAfterEach {
  this: Suite =>

  override implicit def patienceConfig = PatienceConfig(timeout = Span(30000, Millis), interval = Span(250, Millis))

  import JsonSupport.json4sUnmarshaller

  implicit val as = ActorSystem("examples")
  implicit val ec = as.dispatcher

  val esPort = 12000 + (Thread.currentThread.getId % 100).toInt
  val client = new SprayElasticClient("localhost", esPort)

  var es: EmbeddedElasticSearch = _

  override def beforeAll() {
    super.beforeAll()
    es = new EmbeddedElasticSearch(esPort)
    es.start()
  }

  override def afterAll() {
    es.stop()
  }

  def isOk[T] = { _: T => () }

  case class SuccessfulRequest[Resp](check: (Resp => Unit) => Unit)

  def failingWith[T <: Throwable : Manifest](f: Future[_])(implicit p: PatienceConfig, ec: ExecutionContext): T = {
    val at = new AtomicReference[Try[Any]]()

    val w = new Waiter
    f onComplete { case result =>
      at.set(result)
      w.dismiss()
    }
    w.await()(p)

    at.get() match {
      case Success(i) => intercept[T](i)
      case Failure(e) => intercept[T](throw e)
    }
  }

  def successfulRequest[Req, Resp](req: Req)(implicit
      esr: ElasticRequest[Req, Resp],
      fru: FromResponseUnmarshaller[Resp]): SuccessfulRequest[Resp] = {
    val resp = client.execute(req)
    SuccessfulRequest(whenReady(resp)(_: Resp => Unit))
  }

  def failingRequest[Req](req: Req)(implicit esr: ElasticRequest[Req, _]) = {
    failingWith[UnsuccessfulResponse](client.execute(req))
  }

}
