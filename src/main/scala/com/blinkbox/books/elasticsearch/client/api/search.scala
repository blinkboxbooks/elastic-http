package com.blinkbox.books.elasticsearch.client.api

import com.blinkbox.books.elasticsearch.client._
import com.sksamuel.elastic4s.{DeleteIndexDefinition, IndexDefinition}
import com.sksamuel.elastic4s.ElasticDsl.SearchDefinition
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

  implicit object SearchElasticRequest extends ElasticRequest[SearchDefinition, SearchResponse[JValue]] with SearchBase {
    override def request(req: SearchDefinition): HttpRequest = requestFor(req)
  }
}

trait TypedSearchSupport {
  this: SearchSupport =>

  case class TypedSearchDefinition[T: FromResponseUnmarshaller](req: SearchDefinition)

  implicit class SearchDefinitionOps(val req: SearchDefinition) {
    def sourceIs[T: FromResponseUnmarshaller] = TypedSearchDefinition[T](req)
  }

  class TypedSearchElasticRequest[T: FromResponseUnmarshaller]
    extends ElasticRequest[TypedSearchDefinition[T], SearchResponse[T]]
    with SearchBase {

    override def request(req: TypedSearchDefinition[T]): HttpRequest = requestFor(req.req)
  }

  implicit def typedSearchElasticRequest[T: FromResponseUnmarshaller] = new TypedSearchElasticRequest[T]
}
