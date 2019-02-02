package io.leonard

import io.leonard.AnimalSpec._
import io.leonard.TraitFormat.{caseObjectFormat, traitFormat}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json.format
import play.api.libs.json._

class TraitFormatSpec extends FlatSpec with Matchers  {

  "TraitFormat" should "serialise" in {

    val animalFormat = traitFormat[Animal] << format[Dog] << format[Cat]

    val doggyJson = Json.parse("""{"s":"woof!","type":"Dog"}""")
    animalFormat.writes(doggy) should be(doggyJson)

    val animal1: Animal = animalFormat.reads(doggyJson).get
    animal1 should be(doggy)

    val kittyJson = Json.parse("""{"s":"Meow!","type":"Cat"}""")
    animalFormat.writes(kitty) should be(kittyJson)

    val animal2: Animal = animalFormat.reads(kittyJson).get
    animal2 should be(kitty)
  }

  it should "serialise a case object" in {
    val animalFormat = traitFormat[Animal] << format[Dog] << format[Cat] << caseObjectFormat(Nessy)
    val nessyJson    = """{"type":"Nessy"}"""
    animalFormat.writes(Nessy) should be(Json.parse(nessyJson))
    animalFormat.reads(Json.parse(nessyJson)).get should be(Nessy)
  }

  it should "serialise nested" in {
    val mammalFormat = traitFormat[Mammal] << format[Dog] << format[Cat]
    val animalFormat = traitFormat[Animal] << mammalFormat << caseObjectFormat(Nessy)

    val doggyJson = Json.parse("""{"s":"woof!","type":"Dog"}""")
    animalFormat.writes(doggy) should be(doggyJson)

  }

  it should "put discriminator in the JSON" in {
    val animalFormat = traitFormat[Animal]("animalType") << format[Dog] << format[Cat]
    val doggyJson    = """{"s":"woof!","animalType":"Dog"}"""
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
    val doggyJson    = """{"s":"woof!","animalType":{"type":"Dog"}}"""
    val jsResult     = animalFormat.reads(Json.parse(doggyJson))
    jsResult should be(JsError(s"No valid discriminator property 'animalType' found in $doggyJson."))
  }

  it should "custom name for case class format" in {
    val animalFormat = traitFormat[Animal] << ("hound", format[Dog]) << ("pussy_cat", format[Cat])
    val houndJson    = """{"s":"woof!","type":"hound"}"""
    val jsResult     = animalFormat.reads(Json.parse(houndJson))
    jsResult.get should be(doggy)

    val pussyCatJson = """{"s":"Meow!","type":"pussy_cat"}"""
    animalFormat.reads(Json.parse(pussyCatJson)).get should be(kitty)
  }

  it should "write and read custom name for case object format" in {
    val animalFormat = traitFormat[Animal] << ("hound", format[Dog]) << ("pussy_cat", format[Cat]) << ("sea_monster", caseObjectFormat(Nessy))
    val nessyJson    = """{"type":"sea_monster"}"""
    val jsResult     = animalFormat.reads(Json.parse(nessyJson))
    jsResult.get should be(Nessy)

    animalFormat.writes(Nessy).toString() should be(nessyJson)
  }
}
