package com.blinkbox.books.elasticsearch.client

import org.scalatest.{FlatSpec, Matchers}
import spray.httpx.Json4sJacksonSupport

class IndexAndGetSpecs extends FlatSpec with Matchers with ElasticTest {

  import com.blinkbox.books.elasticsearch.client.SprayElasticClientRequests._
  import com.sksamuel.elastic4s.ElasticDsl._
  import JsonSupport.json4sUnmarshaller

  "The ES client" should "be able to ping an ES instance" in {
    whenRequestDone(StatusRequest) check { resp =>
      resp.status should equal(200)
    }
  }

  it should "be able to create an index with some mappings" in {
    whenRequestDone(create index("foo")) check { resp =>
      resp.acknowledged shouldBe true
    }
  }
}
