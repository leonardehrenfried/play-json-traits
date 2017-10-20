package io.leonard

import io.leonard.AnimalSpec._
import io.leonard.TraitFormat.{caseObjectFormat, traitFormat}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json.format
import play.api.libs.json.{JsError, Json}

class SubPropertySerializationStrategySpec extends FlatSpec with Matchers  {

  "TraitFormat with SubProperty serialization strategy" should "serialise" in {

    val animalFormat = traitFormat[Animal](serialisationStrategy = SubProperty) << format[Dog] << format[Cat]

    val doggyJson = """{"value":{"s":"woof!"},"type":"Dog"}"""
    animalFormat.writes(doggy).toString() should be(doggyJson)

    val animal1: Animal = animalFormat.reads(Json.parse(doggyJson)).get
    animal1 should be(doggy)

    val kittyJson = """{"value":{"s":"Meow!"},"type":"Cat"}"""
    animalFormat.writes(kitty).toString() should be(kittyJson)

    val animal2: Animal = animalFormat.reads(Json.parse(kittyJson)).get
    animal2 should be(kitty)
  }

  it should "serialise a case object" in {
    val animalFormat = traitFormat[Animal](serialisationStrategy = SubProperty) << format[Dog] << format[Cat] << caseObjectFormat(Nessy)
    val nessyJson    = """{"value":{},"type":"Nessy"}"""
    animalFormat.writes(Nessy) should be(Json.parse(nessyJson))
    animalFormat.reads(Json.parse(nessyJson)).get should be(Nessy)
  }

  it should "put discriminator in the JSON" in {
    val animalFormat = traitFormat[Animal](new SubProperty("animalType", "value")) << format[Dog] << format[Cat]
    val doggyJson    = """{"value":{"s":"woof!"},"animalType":"Dog"}"""
    animalFormat.writes(doggy).toString() should be(doggyJson)

    val animal1: Animal = animalFormat.reads(Json.parse(doggyJson)).get
    animal1 should be(doggy)
  }

  it should "put discriminator and container name in the JSON" in {
    val animalFormat = traitFormat[Animal](new SubProperty("animalType", "data")) << format[Dog] << format[Cat]
    val doggyJson    = """{"data":{"s":"woof!"},"animalType":"Dog"}"""
    animalFormat.writes(doggy).toString() should be(doggyJson)

    val animal1: Animal = animalFormat.reads(Json.parse(doggyJson)).get
    animal1 should be(doggy)
  }

  it should "return a JsError if the discriminator is not there" in {
    val animalFormat = traitFormat[Animal](SubProperty) << format[Dog] << format[Cat]
    val doggyJson    = """{"value":{"s":"woof!"}}"""
    val jsResult     = animalFormat.reads(Json.parse(doggyJson))
    jsResult should be(JsError(s"No valid discriminator property 'type' found in $doggyJson."))
  }

  it should "return a JsError if the discriminator is not a string" in {
    val animalFormat = traitFormat[Animal](SubProperty) << format[Dog] << format[Cat]
    val doggyJson    = """{"value":{"s":"woof!"},"animalType":{"type":"Dog"}}"""
    val jsResult     = animalFormat.reads(Json.parse(doggyJson))
    jsResult should be(JsError(s"No valid discriminator property 'type' found in $doggyJson."))
  }

  it should "return a JsError if the container is missing" in {
    val animalFormat = traitFormat[Animal](SubProperty) << format[Dog] << format[Cat]
    val doggyJson    = """{"type":"Dog"}"""
    val jsResult     = animalFormat.reads(Json.parse(doggyJson))
    jsResult should be(JsError(s"No valid container property 'value' found in $doggyJson."))
  }

  it should "return a JsError if the container is not an object" in {
    val animalFormat = traitFormat[Animal](SubProperty) << format[Dog] << format[Cat]
    val doggyJson    = """{"value":"woof!","type":"Dog"}"""
    val jsResult     = animalFormat.reads(Json.parse(doggyJson))
    jsResult should be(JsError(s"No valid container property 'value' found in $doggyJson."))
  }

  it should "return a JsError if the container and discriminator are missing" in {
    val animalFormat = traitFormat[Animal](SubProperty) << format[Dog] << format[Cat]
    val doggyJson    = """{"s":"woof!","ff":"R"}"""
    val jsResult     = animalFormat.reads(Json.parse(doggyJson))
    jsResult should be(JsError(s"No valid discriminator property 'type' nor container property 'value' found in $doggyJson."))
  }

  it should "custom name for case class format" in {
    val animalFormat = traitFormat[Animal](SubProperty) << ("hound", format[Dog]) << ("pussy_cat", format[Cat])
    val houndJson    = """{"value":{"s":"woof!"},"type":"hound"}"""
    val jsResult     = animalFormat.reads(Json.parse(houndJson))
    jsResult.get should be(doggy)

    val pussyCatJson = """{"value":{"s":"Meow!"},"type":"pussy_cat"}"""
    animalFormat.reads(Json.parse(pussyCatJson)).get should be(kitty)
  }

  it should "write and read custom name for case object format" in {
    val animalFormat = traitFormat[Animal](SubProperty) << ("hound", format[Dog]) << ("pussy_cat", format[Cat]) << ("sea_monster", caseObjectFormat(Nessy))
    val nessyJson    = """{"value":{},"type":"sea_monster"}"""
    val jsResult     = animalFormat.reads(Json.parse(nessyJson))
    jsResult.get should be(Nessy)

    animalFormat.writes(Nessy).toString() should be(nessyJson)
  }
}
