package io.leonard

import io.leonard.TraitFormat.CaseObjectFormat
import play.api.libs.json._

import scala.reflect.ClassTag

class TraitFormat[Supertype] private (val mapping: Map[String, Format[Supertype]], discriminator: String) extends Format[Supertype]{

  def reads(js: JsValue): JsResult[Supertype] = {
    val name = (js \ discriminator).validate[String]
    name match {
      case JsSuccess(typeName, _) => mapping.get(typeName)
        .map(_.reads(js))
        .getOrElse(JsError(s"Could not find deserialisation format for discriminator '$discriminator' in $js."))
      case _ => JsError(s"No valid discriminator property '$discriminator' found in $js.")
    }
  }

  def writes(f: Supertype): JsValue = {
    val format = mapping.get(stripTrailingDollar(f.getClass))
    format.fold(throw new IllegalArgumentException(s"Could not find format for $f. Trait format contains formats ${mapping.keySet}."))(q => q.writes(f))
  }

  /**
    * @return An immutable copy of the TraitFormat with Subtype added to the list of possible serialisation
    *         formats.
    *
    *         Complete example: val animalFormat = traitFormat[Animal] << format[Cat] << caseObjectFormat(Nessy)
    */
  def <<[Subtype <: Supertype](format: Format[Subtype])(implicit tag: ClassTag[Subtype]):TraitFormat[Supertype] = {
    val newMapping = mapping + (stripTrailingDollar(tag.runtimeClass) -> transform(format))
    new TraitFormat[Supertype](newMapping, discriminator)
  }

  def <<[Subtype <: Supertype](caseObjectFormat: CaseObjectFormat[Subtype])(implicit tag: ClassTag[Subtype]):TraitFormat[Supertype] = {
    <<(caseObjectFormat.format)
  }

  private def transform[Subtype <: Supertype](in: Format[Subtype]): Format[Supertype] = new Format[Supertype] {
    override def writes(o: Supertype): JsValue = in.writes(o.asInstanceOf[Subtype]).as[JsObject] + (discriminator -> JsString(stripTrailingDollar(o.getClass)))
    override def reads(json: JsValue): JsResult[Supertype] = in.reads(json)
  }

  private def stripTrailingDollar(in: Class[_]): String = in.getSimpleName.stripSuffix("$")
}

object TraitFormat {
  val defaultDiscriminator = "type"
  def traitFormat[T]: TraitFormat[T] = traitFormat(defaultDiscriminator)
  def traitFormat[T](discriminator: String): TraitFormat[T] = new TraitFormat[T](Map(), discriminator)

  class CaseObjectFormat[T](private[TraitFormat] val format: Format[T])

  def caseObjectFormat[T](caseObject: T) = new CaseObjectFormat(new Format[T] {
    override def writes(o: T): JsValue = Json.obj()
    override def reads(json: JsValue): JsResult[T] = JsSuccess(caseObject)
  })
}
