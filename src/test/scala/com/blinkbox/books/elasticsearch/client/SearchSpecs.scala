package com.blinkbox.books.elasticsearch.client

import com.blinkbox.books.test.FailHelper
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import org.scalatest.{ FlatSpec, Matchers }
import spray.httpx.Json4sJacksonSupport
import ElasticClientApi._

class SearchSpecs extends FlatSpec with Matchers with ElasticTest {

  import TestFixtures._
  import JsonSupport.json4sUnmarshaller

  override def beforeAll() {
    super.beforeAll()
    successfulRequest(indexDef) check isOk
    successfulRequest(RefreshAllIndices) check isOk
  }

  "The ES HTTP client" should "return an empty result set when searching an empty index" in {
    successfulRequest((search in "catalogue" -> "book" query matchall).sourceIs[Book]) check { resp =>
      resp.hits.total should equal(0)
      resp.hits.hits should be(empty)
    }
  }

  it should "allow performing a query and retrieving some results" in {
    successfulRequest(index into "catalogue" -> "book" id troutBook.isbn doc troutBookSource) check isOk
    successfulRequest(RefreshAllIndices) check isOk
    successfulRequest((search in "catalogue" -> "book" query matchall).sourceIs[Book]) check { resp =>
      resp.hits.total should equal(1)
      resp.hits.hits should contain theSameElementsAs SearchHit("catalogue", "book", troutBook.isbn, troutBook) :: Nil
    }
  }

  it should "support spellcheck suggestions" in {
    successfulRequest(index into "catalogue" -> "book" id troutBook.isbn doc troutBookSource) check isOk
    successfulRequest(RefreshAllIndices) check isOk

    val q = "protolocs of trafalmadore"
    val searchReq = successfulRequest((
      search in "catalogue" -> "book" query (
        matchPhrase("title", q)
      ) suggestions (
        suggest using phrase as "spellcheck" on q from "title" size 1 maxErrors 3
      )).sourceIs[Book])

    searchReq check { resp =>
      resp.hits.total should equal(0)
      resp.hits.hits should be(empty)
      resp.suggest should be(defined)
      resp.suggest foreach { suggestions =>
        suggestions should be definedAt("spellcheck")
        suggestions get "spellcheck" foreach { spellchecks =>
          spellchecks should have size(1)

          val spellcheck = spellchecks.head
          spellcheck.text should equal("protolocs of trafalmadore")
          spellcheck.options should have size(1)
          spellcheck.options.head.text should equal("protocols of tralfamadore")
        }
      }
    }
  }

  it should "support completion suggestions" in {
    successfulRequest(index into "catalogue" -> "book" id troutBook.isbn doc troutBookSource) check isOk
    successfulRequest(RefreshAllIndices) check isOk

    val query = search in "catalogue" -> "book" suggestions (suggest using completion as "completions" on "the pro" from "autoComplete")

    successfulRequest(query.sourceIs[Book].suggestionIs[CompletionPayload]) check { resp =>
      resp.suggest should be(defined)
      resp.suggest foreach { suggestions =>
        val completions = suggestions.getOrElse("completions", Seq.empty)
        completions should have size(1)

        val completion = completions.head
        completion.text should equal("the pro")
        completion.options should have size(1)

        val option = completion.options.head
        option.text should equal("The Protocols of the Elders of Tralfamadore")
        option.payload should equal(Some(troutBook.autoComplete.payload))
      }
    }
  }
}
