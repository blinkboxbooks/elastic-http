package com.blinkbox.books.elasticsearch.client

import com.blinkbox.books.elasticsearch.client.ElasticClientApi._
import com.sksamuel.elastic4s.ElasticDsl._
import org.elasticsearch.index.VersionType
import org.scalatest.{FlatSpec, Matchers}
import spray.http.StatusCodes

class DocumentSpecs extends FlatSpec with Matchers with ElasticTest {

  import com.blinkbox.books.elasticsearch.client.JsonSupport.json4sUnmarshaller
  import com.blinkbox.books.elasticsearch.client.TestFixtures._

  override def beforeAll() {
    super.beforeAll()
  }

  override def beforeEach() {
    super.beforeEach()
    successfulRequest(deleteIndex("_all")) check isOk
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
    successfulRequest(update id troutBook.isbn in "catalogue" -> "book" doc updatedTroutBookSource) check isOk
    successfulRequest((get id troutBook.isbn from "catalogue" -> "book").sourceIs[Book]) check { b =>
      b._id should equal(troutBook.isbn)
      b._type should equal("book")
      b._index should equal("catalogue")
      b._source should equal(Some(updatedTroutBook))
    }
  }

  it should "upsert a document via the update API" in {
    implicit val formats = JsonSupport.json4sFormats
    successfulRequest(update id troutBook.isbn in "catalogue" -> "book" doc troutBookSource docAsUpsert true) check isOk
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
    successfulRequest(update id troutBook.isbn in "catalogue" -> "book" script "ctx._source.title = title" params(Map("title" -> "Maniacs in the Fourth Dimension"))) check isOk
    successfulRequest((get id troutBook.isbn from "catalogue" -> "book").sourceIs[Book]) check { b =>
      b._id should equal(troutBook.isbn)
      b._type should equal("book")
      b._index should equal("catalogue")
      b._source should equal(Some(troutBook.copy(title = "Maniacs in the Fourth Dimension")))
    }
  }

  it should "support bulk operations" in {
    val updatingBook = Book(
      "0987654321098", "The sirens of Titan", "Kilgore Trout", Distribution(true, 3.21))
    val updatingBookSource = BookJsonSource(updatingBook)

    val indexingBook = Book(
      "5555555555555", "Slaughterhouse 5", "Kurt Vonnegut", Distribution(true, 5.00))
    val indexingBookSource = BookJsonSource(indexingBook)

    implicit val formats = JsonSupport.json4sFormats

    successfulRequest(index into "catalogue" -> "book" doc troutBookSource id troutBook.isbn) check isOk
    successfulRequest(index into "catalogue" -> "book" doc updatingBookSource id updatingBook.isbn) check isOk

    successfulRequest(bulk(
      delete id troutBook.isbn from "catalogue" -> "book",
      update id updatingBook.isbn in "catalogue" -> "book" doc updatingBookSource,
      index into "catalogue" -> "book" doc indexingBookSource id indexingBook.isbn
    )) check { res =>
      res.errors shouldBe false
      res.items should contain theSameElementsAs(
        DeleteResponseItem("catalogue", "book", "1234567890123", 2, StatusCodes.OK, true, None) ::
          UpdateResponseItem("catalogue", "book", "0987654321098", StatusCodes.OK, None) ::
          IndexResponseItem("catalogue", "book", "5555555555555", 1, StatusCodes.Created, None) ::
          Nil
      )
    }
  }

  it should "support multi-get requests" in {
    implicit val formats = JsonSupport.json4sFormats

    val firstId = "0000000000001"
    val secondId = "0000000000002"
    val notExistingId = "0000000000003"
    val firstBook = troutBook.copy(isbn = firstId)
    val secondBook = troutBook.copy(isbn = secondId)

    successfulRequest(index into "catalogue" -> "book" doc BookJsonSource(firstBook) id firstId) check isOk
    successfulRequest(index into "catalogue" -> "book" doc BookJsonSource(secondBook) id secondId) check isOk

    successfulRequest(multiget(
      get id firstId from "catalogue/book",
      get id secondId from "catalogue/book",
      get id notExistingId from "catalogue/book"
    ).sourceIs[Book]) check { res =>
      res.docs should have size(3)

      res.docs(0)._id should equal(firstId)
      res.docs(0).found shouldBe true
      res.docs(0)._source should equal(Some(firstBook))

      res.docs(1)._id should equal(secondId)
      res.docs(1).found shouldBe true
      res.docs(1)._source should equal(Some(secondBook))

      res.docs(2)._id should equal(notExistingId)
      res.docs(2).found shouldBe false
      res.docs(2)._source should equal(None)
    }
  }
}
