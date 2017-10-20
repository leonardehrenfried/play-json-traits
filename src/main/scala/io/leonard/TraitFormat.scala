package io.leonard

import io.leonard.TraitFormat.CaseObjectFormat
import play.api.libs.json._

import scala.reflect.ClassTag

class TraitFormat[Supertype] private (serializationStrategy: SerializationStrategy, mapping: Map[Class[_], Mapping[Supertype]]) extends Format[Supertype] {

  def reads(js: JsValue): JsResult[Supertype] = serializationStrategy.reads[Supertype](js, mapping)

  def writes(f: Supertype): JsValue = {
    val format = mapping.get(f.getClass).map(_.format)
    format.fold(throw new IllegalArgumentException(s"Could not find format for $f. Trait format contains formats ${mapping.keySet}."))(q => q.writes(f))
  }

  def <<[Subtype <: Supertype](customName: String, format: Format[Subtype])(implicit tag: ClassTag[Subtype]): TraitFormat[Supertype] = {
    val newMapping = mapping + (tag.runtimeClass -> Mapping(customName, transform(customName, format)))
    new TraitFormat[Supertype](serializationStrategy, newMapping)
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
    override def writes(o: Supertype): JsValue             = serializationStrategy.writes(o, name, in)
    override def reads(json: JsValue): JsResult[Supertype] = in.reads(json)
  }

  private def getNameFromClass(in: Class[_]): String = in.getSimpleName.stripSuffix("$")
}

object TraitFormat {
  def traitFormat[T]: TraitFormat[T]                                               = traitFormat(serialisationStrategy = MergedObject)
  def traitFormat[T](discriminator: String): TraitFormat[T]                        = traitFormat(serialisationStrategy = new MergedObject(discriminator))
  def traitFormat[T](serialisationStrategy: SerializationStrategy): TraitFormat[T] = new TraitFormat[T](serialisationStrategy, Map())

  class CaseObjectFormat[T](private[TraitFormat] val format: Format[T])

  def caseObjectFormat[T](caseObject: T) =
    new CaseObjectFormat(new Format[T] {
      override def writes(o: T): JsValue             = Json.obj()
      override def reads(json: JsValue): JsResult[T] = JsSuccess(caseObject)
    })
}
