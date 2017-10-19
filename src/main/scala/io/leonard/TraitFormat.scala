package io.leonard

import io.leonard.TraitFormat.CaseObjectFormat
import play.api.libs.json._

import scala.reflect.ClassTag

private case class Mapping[A](
  name: String,
  format: Format[A]
)

class TraitFormat[Supertype] private (mapping: Map[Class[_], Mapping[Supertype]], discriminatorProperty: String, containerProperty: String)
  extends Format[Supertype] {

  def reads(js: JsValue): JsResult[Supertype] = {
    val name = (js \ discriminatorProperty).validate[String]
    val content = (js \ containerProperty).validate[JsValue]
    (name, content) match {
      case (JsSuccess(extractedName, _), JsSuccess(extractedContent, _)) =>
        mapping.values
          .find(_.name == extractedName)
          .map(_.format)
          .map(_.reads(extractedContent))
          .getOrElse(JsError(s"Could not find deserialisation format for discriminator '$discriminatorProperty' in $js."))
      case (JsError(_), _) => JsError(s"No valid discriminator property '$discriminatorProperty' found in $js.")
      case (_, JsError(_)) => JsError(s"No valid container property '$containerProperty' found in $js.")
    }
  }

  def writes(f: Supertype): JsValue = {
    val format = mapping.get(f.getClass).map(_.format)
    format.fold(throw new IllegalArgumentException(s"Could not find format for $f. Trait format contains formats ${mapping.keySet}."))(q => q.writes(f))
  }

  def <<[Subtype <: Supertype](customName: String, format: Format[Subtype])(implicit tag: ClassTag[Subtype]): TraitFormat[Supertype] = {
    val newMapping = mapping + (tag.runtimeClass -> Mapping(customName, transform(customName, format)))
    new TraitFormat[Supertype](newMapping, discriminatorProperty, containerProperty)
  }

  /**
    * @return An immutable copy of the TraitFormat with Subtype added to the list of possible serialisation
    *         formats.
    *
    *         Complete example: val animalFormat = traitFormat[Animal] << format[Cat] << caseObjectFormat(Nessy)
    */
  def <<[Subtype <: Supertype](format: Format[Subtype])(implicit tag: ClassTag[Subtype]): TraitFormat[Supertype] =
    <<(getNameFromClass(tag.runtimeClass), format)

  def <<[Subtype <: Supertype](caseObjectFormat: CaseObjectFormat[Subtype])(implicit tag: ClassTag[Subtype]): TraitFormat[Supertype] =
    <<(caseObjectFormat.format)

  def <<[Subtype <: Supertype](customName: String, caseObjectFormat: CaseObjectFormat[Subtype])(implicit tag: ClassTag[Subtype]): TraitFormat[Supertype] =
    <<(customName, caseObjectFormat.format)

  private def transform[Subtype <: Supertype](name: String, in: Format[Subtype]): Format[Supertype] = new Format[Supertype] {
    override def writes(o: Supertype): JsValue = Json.obj(
      containerProperty -> in.writes(o.asInstanceOf[Subtype]).as[JsObject],
      discriminatorProperty -> JsString(name)
    )
    override def reads(json: JsValue): JsResult[Supertype] = in.reads(json)
  }

  private def getNameFromClass(in: Class[_]): String = in.getSimpleName.stripSuffix("$")
}

object TraitFormat {
  val defaultDiscriminator                                  = "name"
  val defaultContainer                                      = "value"
  def traitFormat[T]: TraitFormat[T]                        = traitFormat(defaultDiscriminator, defaultContainer)
  def traitFormat[T](discriminator: String): TraitFormat[T] = new TraitFormat[T](Map(), discriminator, defaultContainer)
  def traitFormat[T](discriminator: String, container: String): TraitFormat[T] = new TraitFormat[T](Map(), discriminator, container)

  class CaseObjectFormat[T](private[TraitFormat] val format: Format[T])

  def caseObjectFormat[T](caseObject: T) =
    new CaseObjectFormat(new Format[T] {
      override def writes(o: T): JsValue             = Json.obj()
      override def reads(json: JsValue): JsResult[T] = JsSuccess(caseObject)
    })
}
