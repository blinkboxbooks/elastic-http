package com.blinkbox.books.elasticsearch.client

import akka.actor.ActorSystem
import com.sksamuel.elastic4s.IndexDefinition
import com.sksamuel.elastic4s.source.StringDocumentSource
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JValue
import spray.httpx.Json4sJacksonSupport

import scala.concurrent.Future

object Examples {

  case class Book(isbn: String)

  object JsonSupport extends Json4sJacksonSupport {
    implicit val json4sJacksonFormats = DefaultFormats
  }

  implicit val as = ActorSystem("examples")
  implicit val ec = as.dispatcher

  val client = new SprayElasticClient("localhost", 9200)

  import com.blinkbox.books.elasticsearch.client.SprayElasticClientRequests._
  import com.sksamuel.elastic4s.ElasticDsl._
  import JsonSupport.json4sUnmarshaller

  val indexReq = index
    .into(s"catalogue/book")
    .doc(StringDocumentSource("""{ "isbn": "1234567890123" }"""))
    .id("1234567890123")

  def putBook(): Future[IndexResponse] = client.execute[IndexDefinition, IndexResponse](indexReq)

  def getBook(): Future[GetResponse[JValue]] = client.execute(get("9780307794147").from("catalogue", "book"))

  def getTypedBook(): Future[GetResponse[Book]] = client.execute(get("9780307794147").from("catalogue", "book").sourceIs[Book])

  def shutdown() = as.shutdown()
}
