package io.leonard

import play.api.libs.json._

/**
  * A serializationStrategy is responsible of serialized JSON content organisation
  */
sealed trait SerializationStrategy {
  def reads[Supertype](js: JsValue, mapping: Map[Class[_], ClassMapping[Supertype]]): JsResult[Supertype]
  def writes[Supertype, Subtype](o: Supertype, name: String, in: Format[Subtype]): JsValue
}

/**
  * SubProperty strategy will format JSON like this
  * {
  *   "value": {
  *     "prop1": "Example content"
  *   },
  *   "type": "Cat"
  * }
  *
  * @param discriminatorProperty
  * @param containerProperty
  */
case class SubProperty(discriminatorProperty: String, containerProperty: String) extends SerializationStrategy {
  override def reads[Supertype](js: JsValue, mapping: Map[Class[_], ClassMapping[Supertype]]): JsResult[Supertype] = {
    val discriminator = (js \ discriminatorProperty).validate[String]
    val content       = (js \ containerProperty).validate[JsObject]
    (discriminator, content) match {
      case (JsSuccess(extractedName, _), JsSuccess(extractedContent, _)) =>
        mapping.values
          .find(_.name == extractedName)
          .map(_.format)
          .map(_.reads(extractedContent))
          .getOrElse(JsError(s"Could not find deserialisation format for discriminator '$discriminatorProperty' in $js."))
      case (JsError(_), JsSuccess(_, _)) => JsError(s"No valid discriminator property '$discriminatorProperty' found in $js.")
      case (JsSuccess(_, _), JsError(_)) => JsError(s"No valid container property '$containerProperty' found in $js.")
      case (JsError(_), JsError(_)) =>
        JsError(s"No valid discriminator property '$discriminatorProperty' nor container property '$containerProperty' found in $js.")
    }
  }
  def writes[Supertype, Subtype](o: Supertype, name: String, in: Format[Subtype]): JsValue = Json.obj(
    containerProperty     -> in.writes(o.asInstanceOf[Subtype]).as[JsObject],
    discriminatorProperty -> JsString(name)
  )
}

object SubProperty extends SubProperty("type", "value") {}

/**
  * MergedObject strategy will format JSON like this
  * {
  *   "prop1": "Example content",
  *   "type": "Cat"
  * }
  *
  * With "type" being the default discriminator merged with the serialized object properties
  *
  * @param discriminatorProperty
  */
case class MergedObject(discriminatorProperty: String) extends SerializationStrategy {
  override def reads[Supertype](js: JsValue, mapping: Map[Class[_], ClassMapping[Supertype]]): JsResult[Supertype] = {
    val name = (js \ discriminatorProperty).validate[String]
    name match {
      case JsSuccess(extractedName, _) =>
        mapping.values
          .find(_.name == extractedName)
          .map(_.format)
          .map(_.reads(js))
          .getOrElse(JsError(s"Could not find deserialisation format for discriminator '$discriminatorProperty' in $js."))
      case _ => JsError(s"No valid discriminator property '$discriminatorProperty' found in $js.")
    }
  }

  override def writes[Supertype, Subtype](o: Supertype, name: String, in: Format[Subtype]): JsValue =
    in.writes(o.asInstanceOf[Subtype]).as[JsObject] + (discriminatorProperty -> JsString(name))
}

object MergedObject extends MergedObject("type")
