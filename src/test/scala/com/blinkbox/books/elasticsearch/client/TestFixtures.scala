package com.blinkbox.books.elasticsearch.client

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import com.sksamuel.elastic4s.source.DocumentSource
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization._
import spray.httpx.Json4sJacksonSupport

object TestFixtures {
  case class Distribution(distributed: Boolean, price: Double)

  case class CompletionPayload(isbn: String, title: String)
  case class Completion(input: List[String], output: String, payload: CompletionPayload)
  object Completion {
    def apply(isbn: String, title: String): Completion = Completion(title :: Nil, title, CompletionPayload(isbn, title))
  }

  case class Book(isbn: String, title: String, author: String, distribution: Distribution, autoComplete: Completion)
  object Book {
    def apply(isbn: String, title: String, author: String, distribution: Distribution): Book = Book(isbn, title, author, distribution, Completion(isbn, title))
  }

  case class BookJsonSource(book: Book) extends DocumentSource {
    import JsonSupport._
    def json = write(book)
  }

  val troutBook = Book(
    "1234567890123", "The Protocols of the Elders of Tralfamadore", "Kilgore Trout", Distribution(true, 1.23))

  val troutBookSource = BookJsonSource(troutBook)

  val indexDef = create index("catalogue") mappings (
    "book" as (
      "isbn" typed StringType,
      "title" typed StringType,
      "author" typed StringType,
      "distribution" inner (
        "distributed" typed BooleanType,
        "price" typed DoubleType
      ),
      "autoComplete" typed CompletionType payloads true
    ) dynamic false
  )
}
