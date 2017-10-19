package io.leonard

import io.leonard.TraitFormat.{traitFormat, caseObjectFormat}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json.format
import play.api.libs.json._

class TraitFormatSpec extends FlatSpec with Matchers {

  sealed trait Animal
  case class Dog(s: String) extends Animal
  case class Cat(s: String) extends Animal
  case object Nessy         extends Animal

  val doggy = Dog("woof!")
  val kitty = Cat("Meow!")

  "TraitFormat" should "serialise" in {

    val animalFormat = traitFormat[Animal] << format[Dog] << format[Cat]

    val doggyJson = """{"value":{"s":"woof!"},"name":"Dog"}"""
    animalFormat.writes(doggy).toString() should be(doggyJson)

    val animal1: Animal = animalFormat.reads(Json.parse(doggyJson)).get
    animal1 should be(doggy)

    val kittyJson = """{"value":{"s":"Meow!"},"name":"Cat"}"""
    animalFormat.writes(kitty).toString() should be(kittyJson)

    val animal2: Animal = animalFormat.reads(Json.parse(kittyJson)).get
    animal2 should be(kitty)
  }

  it should "serialise a case object" in {
    val animalFormat = traitFormat[Animal] << format[Dog] << format[Cat] << caseObjectFormat(Nessy)
    val nessyJson    = """{"value":{},"name":"Nessy"}"""
    animalFormat.writes(Nessy) should be(Json.parse(nessyJson))
    animalFormat.reads(Json.parse(nessyJson)).get should be(Nessy)
  }

  it should "put discriminator in the JSON" in {
    val animalFormat = traitFormat[Animal]("animalType") << format[Dog] << format[Cat]
    val doggyJson    = """{"value":{"s":"woof!"},"animalType":"Dog"}"""
    animalFormat.writes(doggy).toString() should be(doggyJson)

    val animal1: Animal = animalFormat.reads(Json.parse(doggyJson)).get
    animal1 should be(doggy)
  }

  it should "put discriminator and container name in the JSON" in {
    val animalFormat = traitFormat[Animal]("animalType", "data") << format[Dog] << format[Cat]
    val doggyJson    = """{"data":{"s":"woof!"},"animalType":"Dog"}"""
    animalFormat.writes(doggy).toString() should be(doggyJson)

    val animal1: Animal = animalFormat.reads(Json.parse(doggyJson)).get
    animal1 should be(doggy)
  }

  it should "return a JsError if the discriminator is not there" in {
    val animalFormat = traitFormat[Animal]("animalType") << format[Dog] << format[Cat]
    val doggyJson    = """{"s":"woof!"}"""
    val jsResult     = animalFormat.reads(Json.parse(doggyJson))
    jsResult should be(JsError(s"No valid discriminator property 'animalType' found in $doggyJson."))
  }

  it should "return a JsError if the discriminator is not a string" in {
    val animalFormat = traitFormat[Animal]("animalType") << format[Dog] << format[Cat]
    val doggyJson    = """{"value":{"s":"woof!"},"animalType":{"type":"Dog"}}"""
    val jsResult     = animalFormat.reads(Json.parse(doggyJson))
    jsResult should be(JsError(s"No valid discriminator property 'animalType' found in $doggyJson."))
  }

  it should "return a JsError if the container is not there" in {
    val animalFormat = traitFormat[Animal]("animalType", "data") << format[Dog] << format[Cat]
    val doggyJson    = """{"s":"woof!","animalType":{"type":"Dog"}}"""
    val jsResult     = animalFormat.reads(Json.parse(doggyJson))
    jsResult should be(JsError(s"No valid discriminator property 'animalType' found in $doggyJson."))
  }

  it should "custom name for case class format" in {
    val animalFormat = traitFormat[Animal] << ("hound", format[Dog]) << ("pussy_cat", format[Cat])
    val houndJson    = """{"value":{"s":"woof!"},"name":"hound"}"""
    val jsResult     = animalFormat.reads(Json.parse(houndJson))
    jsResult.get should be(doggy)

    val pussyCatJson = """{"value":{"s":"Meow!"},"name":"pussy_cat"}"""
    animalFormat.reads(Json.parse(pussyCatJson)).get should be(kitty)
  }

  it should "write and read custom name for case object format" in {
    val animalFormat = traitFormat[Animal] << ("hound", format[Dog]) << ("pussy_cat", format[Cat]) << ("sea_monster", caseObjectFormat(Nessy))
    val nessyJson    = """{"value":{},"name":"sea_monster"}"""
    val jsResult     = animalFormat.reads(Json.parse(nessyJson))
    jsResult.get should be(Nessy)

    animalFormat.writes(Nessy).toString() should be(nessyJson)
  }
}
