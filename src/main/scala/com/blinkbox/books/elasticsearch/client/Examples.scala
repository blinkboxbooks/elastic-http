package com.blinkbox.books.elasticsearch.client

import akka.actor.ActorSystem
import com.sksamuel.elastic4s.source.StringDocumentSource
import org.json4s.DefaultFormats
import spray.httpx.Json4sJacksonSupport

object Examples {

  object JsonSupport extends Json4sJacksonSupport {
    implicit val json4sJacksonFormats = DefaultFormats
  }

  implicit val as = ActorSystem("examples")
  implicit val ec = as.dispatcher

  val client = new SprayElasticClient("localhost", 9200)

  import com.blinkbox.books.elasticsearch.client.ElasticClient._
  import com.sksamuel.elastic4s.ElasticDsl._

  val indexReq = index
    .into(s"catalogue/book")
    .doc(StringDocumentSource("""{ "isbn": "1234567890123" }"""))
    .id("1234567890123")

  def putBook() = client.execute(indexReq)

  def getBook = client.execute(get("9780307794147").from("catalogue", "book"))

  def shutdown() = as.shutdown()
}
