package com.blinkbox.books.elasticsearch.client

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._

object TestFixtures {
  case class Distribution(distributed: Boolean, price: Double)
  case class Book(isbn: String, title: String, author: String, distribution: Distribution)

  val aBook = Book("1234567890123", "A book", "An author", Distribution(true, 1.23))

  val indexDef = create index("catalogue") mappings (
    "book" as (
      "isbn" typed StringType,
      "title" typed StringType,
      "author" typed StringType,
      "distribution" inner (
        "distributed" typed BooleanType,
        "price" typed DoubleType
      )
    ) dynamic false
  )
}
