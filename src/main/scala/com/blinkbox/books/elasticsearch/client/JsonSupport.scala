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
      case _: BulkResponseItem => sys.error("Serialisation of BulkResponseItem is not supported")
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

  val all = BulkResponseItemFormat :: StatusCodeFormat :: Nil
}

private[client] object JsonSupport extends Json4sJacksonSupport {
  implicit val json4sJacksonFormats = DefaultFormats ++ Formats.all
}
