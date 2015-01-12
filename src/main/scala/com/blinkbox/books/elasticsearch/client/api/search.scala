package com.blinkbox.books.elasticsearch.client.api

import com.blinkbox.books.elasticsearch.client._
import com.sksamuel.elastic4s.SearchDefinition
import org.elasticsearch.action.search.SearchRequest
import org.json4s.JValue
import spray.client.pipelining._
import spray.http.{HttpRequest, Uri}
import spray.httpx.unmarshalling.FromResponseUnmarshaller

trait SearchSupport {
  sealed trait SearchBase {
    def urlFor(req: SearchRequest) = s"/${req.indices.mkString(",")}/${req.types.mkString(",")}/_search"

    def paramsFor(request: SearchRequest): Map[String, String] =
      Option(request.routing).fold(Map.empty[String, String])(r => Map("routing" -> r))

    def requestFor(req: SearchDefinition): HttpRequest = {
      val builtRequest = req.build
      val uri = Uri(urlFor(builtRequest)).withQuery(paramsFor(builtRequest))

      Post(uri, req._builder.toString)
    }
  }

  implicit object SearchElasticRequest extends ElasticRequest[SearchDefinition, SearchResponse[JValue, JValue]] with SearchBase {
    override def request(req: SearchDefinition): HttpRequest = requestFor(req)
  }
}

trait TypedSearchSupport {
  this: SearchSupport =>

  case class TypedSearchDefinition[Document: FromResponseUnmarshaller, Suggestion: FromResponseUnmarshaller](req: SearchDefinition) {
    def sourceIs[T: FromResponseUnmarshaller] = TypedSearchDefinition[T, Suggestion](req)
    def suggestionIs[T: FromResponseUnmarshaller] = TypedSearchDefinition[Document, T](req)
  }

  implicit class SearchDefinitionOps(val req: SearchDefinition) {
    def sourceIs[T: FromResponseUnmarshaller](implicit um: FromResponseUnmarshaller[JValue]) = TypedSearchDefinition[T, JValue](req)
    def suggestionIs[T: FromResponseUnmarshaller](implicit um: FromResponseUnmarshaller[JValue]) = TypedSearchDefinition[JValue, T](req)
  }

  class TypedSearchElasticRequest[Document: FromResponseUnmarshaller, Suggestion: FromResponseUnmarshaller]
    extends ElasticRequest[TypedSearchDefinition[Document, Suggestion], SearchResponse[Document, Suggestion]]
    with SearchBase {

    override def request(req: TypedSearchDefinition[Document, Suggestion]): HttpRequest = requestFor(req.req)
  }

  implicit def typedSearchElasticRequest[Document: FromResponseUnmarshaller, Suggestion: FromResponseUnmarshaller] = new TypedSearchElasticRequest[Document, Suggestion]
}
