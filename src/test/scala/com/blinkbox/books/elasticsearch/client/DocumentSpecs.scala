package com.blinkbox.books.elasticsearch.client

import com.blinkbox.books.test.FailHelper
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import org.elasticsearch.index.VersionType
import org.scalatest.{ FlatSpec, Matchers }
import spray.httpx.Json4sJacksonSupport
import spray.http.StatusCodes

import ElasticClientApi._

class DocumentSpecs extends FlatSpec with Matchers with ElasticTest {

  import TestFixtures._
  import JsonSupport.json4sUnmarshaller

  override def beforeAll() {
    super.beforeAll()
    successfulRequest(indexDef) check isOk
  }

  "The ES client" should "get a 404 when getting a non-existing document" in {
    failingRequest(get id troutBook.isbn from "catalogue" -> "book").statusCode should equal(StatusCodes.NotFound)
  }

  it should "index a document providing an id and be able to retrieve it" in {
    successfulRequest(index into "catalogue" -> "book" doc troutBookSource id troutBook.isbn) check isOk
    successfulRequest((get id troutBook.isbn from "catalogue" -> "book").sourceIs[Book]) check { b =>
      b._id should equal(troutBook.isbn)
      b._type should equal("book")
      b._index should equal("catalogue")
      b._source should equal(Some(troutBook))
    }
  }

  it should "index a document providing an id and an external version and be able to retrieve it" in {
    val req = index into "catalogue" -> "book" doc troutBookSource id troutBook.isbn versionType VersionType.EXTERNAL version 100

    successfulRequest(req) check { idxResp =>
      successfulRequest((get id troutBook.isbn from "catalogue" -> "book").sourceIs[Book]) check { getResp =>
        getResp._version should equal(Some(100))
      }
    }
  }

  it should "index a document not providing an id by generating one from ES" in {
    successfulRequest(index into "catalogue" -> "book" doc troutBookSource) check { idxResp =>
      idxResp._id should not equal ("")
      successfulRequest((get id idxResp._id from "catalogue" -> "book").sourceIs[Book]) check { getResp =>
        getResp._id should equal(idxResp._id)
        getResp._source should equal(Some(troutBook))
      }
    }
  }

  it should "index a document and delete it when requested" in {
    successfulRequest(index into "catalogue" -> "book" doc troutBookSource id troutBook.isbn) check isOk
    successfulRequest(delete id troutBook.isbn from "catalogue" -> "book") check { deleteResp =>
      deleteResp.found shouldBe true
      deleteResp._id should equal(troutBook.isbn)
      deleteResp._index should equal("catalogue")
      deleteResp._type should equal("book")
    }
  }

  it should "index a document providing an id and be able to update it providing a replacemente doc" in {
    implicit val formats = JsonSupport.json4sFormats
    val updatedTroutBook = troutBook.copy(title = "Maniacs in the Fourth Dimension")
    val updatedTroutBookSource = BookJsonSource(updatedTroutBook)
    successfulRequest(index into "catalogue" -> "book" doc troutBookSource id troutBook.isbn) check isOk
    successfulRequest(update(troutBook.isbn) in "catalogue" -> "book" doc updatedTroutBookSource) check isOk
    successfulRequest((get id troutBook.isbn from "catalogue" -> "book").sourceIs[Book]) check { b =>
      b._id should equal(troutBook.isbn)
      b._type should equal("book")
      b._index should equal("catalogue")
      b._source should equal(Some(updatedTroutBook))
    }
  }

  it should "upsert a document via the update API" in {
    implicit val formats = JsonSupport.json4sFormats
    successfulRequest(update(troutBook.isbn) in "catalogue" -> "book" doc troutBookSource docAsUpsert true) check isOk
    successfulRequest((get id troutBook.isbn from "catalogue" -> "book").sourceIs[Book]) check { b =>
      b._id should equal(troutBook.isbn)
      b._type should equal("book")
      b._index should equal("catalogue")
      b._source should equal(Some(troutBook))
    }
  }

  it should "index a document providing an id and be able to update it using the script parameter" in {
    implicit val formats = JsonSupport.json4sFormats
    successfulRequest(index into "catalogue" -> "book" doc troutBookSource id troutBook.isbn) check isOk
    successfulRequest(update(troutBook.isbn) in "catalogue" -> "book" script "ctx._source.title = title" params(Map("title" -> "Maniacs in the Fourth Dimension"))) check isOk
    successfulRequest((get id troutBook.isbn from "catalogue" -> "book").sourceIs[Book]) check { b =>
      b._id should equal(troutBook.isbn)
      b._type should equal("book")
      b._index should equal("catalogue")
      b._source should equal(Some(troutBook.copy(title = "Maniacs in the Fourth Dimension")))
    }
  }
}
