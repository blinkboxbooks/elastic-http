package com.blinkbox.books.elasticsearch.client

import org.json4s.DefaultFormats
import org.json4s._
import org.json4s.Serializer
import spray.httpx.Json4sJacksonSupport
import spray.http.{StatusCode, StatusCodes}

object Formats {
  object BulkResponseItemFormat extends Serializer[BulkResponseItem] {
    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), BulkResponseItem] = {
      case (t, JObject(JField("update", v) :: Nil)) if t.clazz == classOf[BulkResponseItem] =>
        v.extract[UpdateResponseItem]
      case (t, JObject(JField("delete", v) :: Nil)) if t.clazz == classOf[BulkResponseItem] =>
        v.extract[DeleteResponseItem]
      case (t, JObject(JField("index", v) :: Nil)) if t.clazz == classOf[BulkResponseItem] =>
        v.extract[IndexResponseItem]
    }

    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case _: BulkResponseItem => throw new UnsupportedOperationException("Serialisation of BulkResponseItem is not supported")
    }
  }

  object StatusCodeFormat extends Serializer[StatusCode] {
    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), StatusCode] = {
      case (t, JInt(code)) if t.clazz == classOf[StatusCode] =>
        StatusCodes.getForKey(code.toInt).getOrElse(sys.error(s"Status code not recognised: $code"))
    }
    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case x: StatusCode => JInt(x.intValue)
    }
  }

  object SourceParameterFormat extends Serializer[api.SourceParameter] {
    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), api.SourceParameter] = {
      case (t, _) if t.clazz == classOf[api.SourceParameter] => throw new UnsupportedOperationException("De-serialising mult-get source parameters is not supported")
    }
    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case api.NotIncludedParameter => JBool(false)
      case api.IncludeExcludeParameter(includes, excludes) =>
        JObject(
          JField("include", JArray(includes.map(JString).toList)),
          JField("exclude", JArray(excludes.map(JString).toList)))
    }
  }

  val all = BulkResponseItemFormat :: StatusCodeFormat :: SourceParameterFormat :: Nil
}

private[client] object JsonSupport extends Json4sJacksonSupport {
  implicit val json4sJacksonFormats = DefaultFormats ++ Formats.all
}
