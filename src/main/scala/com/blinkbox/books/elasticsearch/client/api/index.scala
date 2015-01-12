package com.blinkbox.books.elasticsearch.client.api

import com.blinkbox.books.elasticsearch.client._
import com.sksamuel.elastic4s.{CreateIndexDefinition, DeleteIndexDefinition}
import spray.client.pipelining._
import spray.http.{HttpEntity, HttpRequest, MediaRanges}
import spray.httpx.PipelineException
import spray.httpx.unmarshalling.Unmarshaller

trait CreateIndexSupport {
  implicit object CreateIndexElasticRequest extends ElasticRequest[CreateIndexDefinition, AcknowledgedResponse] {
    override def request(req: CreateIndexDefinition): HttpRequest = {
      val builtRequest = req.build
      Put(s"/${builtRequest.indices.mkString(",")}", req._source.string())
    }
  }
}

trait DeleteIndexSupport {
  implicit object DeleteIndexElasticRequest extends ElasticRequest[DeleteIndexDefinition, AcknowledgedResponse] {
    override def request(req: DeleteIndexDefinition): HttpRequest = {
      val builtRequest = req.build
      Delete(s"/${builtRequest.indices.mkString(",")}")
    }
  }
}

trait RefreshIndicesSupport {
  case class RefreshIndices(indices: Iterable[String])
  val RefreshAllIndices = RefreshIndices("_all" :: Nil)

  implicit object RefreshIndicesElasticRequest extends ElasticRequest[RefreshIndices, RefreshIndicesResponse] {
    override def request(req: RefreshIndices): HttpRequest = Post(s"/${req.indices.mkString(",")}/_refresh")
  }
}

trait CheckExistenceSupport {
  case class CheckExistence(index: String, `type`: Option[String] = None)

  implicit val UnitUnmarshaller = Unmarshaller[Unit](MediaRanges.`*/*`) {
    case HttpEntity.Empty => ()
    case _ => throw new PipelineException("Expected no-content in the response")
  }

  implicit object CheckExistenceElasticRequest extends ElasticRequest[CheckExistence, Unit] {
    override def request(req: CheckExistence): HttpRequest = {
      Head(s"/${req.index}${req.`type`.fold("")(t => s"/${t}")}")
    }
  }
}

trait StatusSupport {
  case object StatusRequest

  implicit object StatusElasticRequest extends ElasticRequest[StatusRequest.type, StatusResponse] {
    override def request(req: StatusRequest.type): HttpRequest = Get("/")
  }
}
