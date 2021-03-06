package com.blinkbox.books.elasticsearch.client

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import org.scalatest.{FlatSpec, Matchers}
import spray.http.StatusCodes
import spray.httpx.Json4sJacksonSupport

import ElasticClientApi._

class IndexSpecs extends FlatSpec with Matchers with ElasticTest {

  import TestFixtures._
  import JsonSupport.json4sUnmarshaller

  "The ES client" should "be able to ping an ES instance" in {
    successfulRequest(StatusRequest) check { resp =>
      resp.status should equal(200)
    }
  }

  it should "be able to check that an index and a type do not exist" in {
    failingRequest(CheckExistence("catalogue")).statusCode should equal(StatusCodes.NotFound)
    failingRequest(CheckExistence("catalogue", Some("book"))).statusCode should equal(StatusCodes.NotFound)
  }

  it should "be able to create and delete an index and some mappings for a type" in {
    successfulRequest(indexDef) check { resp =>
      resp.acknowledged shouldBe true
    }

    successfulRequest(CheckExistence("catalogue")) check isOk
    successfulRequest(CheckExistence("catalogue", Some("book"))) check isOk

    successfulRequest(delete index("catalogue")) check isOk
    failingRequest(CheckExistence("catalogue")).statusCode should equal(StatusCodes.NotFound)
    failingRequest(CheckExistence("catalogue", Some("book"))).statusCode should equal(StatusCodes.NotFound)
  }
}
