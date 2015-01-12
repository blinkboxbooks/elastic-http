package com.blinkbox.books.elasticsearch.client.api

sealed trait SourceParameter
case object NotIncludedParameter extends SourceParameter
case class IncludeExcludeParameter(include: Seq[String], exclude: Seq[String] = Seq.empty) extends SourceParameter

case class MultiGetRequestDoc(_index: String, _type: String, _id: String, _fields: Seq[String], _routing: String, _source: Option[SourceParameter])
case class MultiGetRequest(docs: Seq[MultiGetRequestDoc])
