package io.leonard.playJsonTraits

import play.api.data.validation.ValidationError
import play.api.libs.json._

class JsonProtocolSpec extends FlatSpec with Matchers with BaseJsonProtocol {

  "TraitFormat" should "serialise" in {

    val animalFormat = traitFormat[Animal] << format[Dog] << format[Cat]

    val doggyJson = """{"s":"woof!","tpe":"Dog"}"""
    animalFormat.writes(doggy).toString() should be(doggyJson)

    val animal1: Animal = animalFormat.reads(Json.parse(doggyJson)).get
    animal1 should be(doggy)

    val kittyJson = """{"s":"Meow!","tpe":"Cat"}"""
    animalFormat.writes(kitty).toString() should be(kittyJson)

    val animal2: Animal = animalFormat.reads(Json.parse(kittyJson)).get
    animal2 should be(kitty)
  }

  it should "serialise a case object" in {
    val animalFormat = traitFormat[Animal] << format[Dog] << format[Cat] << caseObjectFormat(Nessy)
    val nessyJson = """{"tpe":"Nessy"}"""
    animalFormat.writes(Nessy) should be(Json.parse(nessyJson))
    animalFormat.reads(Json.parse(nessyJson)).get should be(Nessy)
  }

  it should "put discriminator in the JSON" in {
    val animalFormat = traitFormat[Animal]("animalType") << format[Dog] << format[Cat]
    val doggyJson = """{"s":"woof!","animalType":"Dog"}"""
    animalFormat.writes(doggy).toString() should be(doggyJson)

    val animal1: Animal = animalFormat.reads(Json.parse(doggyJson)).get
    animal1 should be(doggy)
  }

  it should "return a JsError if the discriminator is not there" in {
    val animalFormat = traitFormat[Animal]("animalType") << format[Dog] << format[Cat]
    val doggyJson = """{"s":"woof!"}"""
    val jsResult = animalFormat.reads(Json.parse(doggyJson))
    jsResult should be(JsError(s"Discriminator property 'animalType' not found in $doggyJson."))
  }

  it should "return a JsError if the discriminator is not a string" in {
    val animalFormat = traitFormat[Animal]("animalType") << format[Dog] << format[Cat]
    val doggyJson = """{"s":"woof!","animalType":{"type":"Dog"}}"""
    val jsResult = animalFormat.reads(Json.parse(doggyJson))
    jsResult should be(JsError("Discriminator property 'animalType' must be a string."))
  }
}

