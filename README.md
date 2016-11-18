# play-json-traits

[![Build Status](https://travis-ci.org/leonardehrenfried/play-json-traits.svg?branch=master)](https://travis-ci.org/leonardehrenfried/play-json-traits)

This package allows you read and write traits using `play-json`. It does this
by adding a discriminator property to the JSON which can be used to identify
the precise implementation when parsing the JSON back to Scala.

## Installation

The library is deployed to Maven Central.

Add the following to your `build.sbt`:

```
"io.leonard" % "play-json-traits" %% "1.0.1"
```

## Example usage

```scala
import io.leonard.TraitFormat.{ traitFormat, caseObjectFormat }
import play.api.libs.json.Json.format
import play.api.libs.json._

sealed trait Animal
case class Dog(s: String) extends Animal
case class Cat(s: String) extends Animal
case object Nessy extends Animal

val doggy = Dog("woof!")
val kitty = Cat("Meow!")

val animalFormat = traitFormat[Animal] << format[Dog] << format[Cat] << caseObjectFormat(Nessy)

animalFormat.writes(doggy).toString

// returns {"s":"woof!","type":"Dog"}

val animal1: Animal = animalFormat.reads(Json.parse(doggyJson)).get
animal1 == doggy

animalFormat.writes(kitty).toString() == """{"s":"Meow!","type":"Cat"}"""

```

## Customisation

By default the discriminator property is called `type`. However, this can
be customised by doing the following:

```scala
val animalFormat = traitFormat[Animal]("animalType") << format[Dog] << format[Cat]
animalFormat.writes(doggy).toString() == """{"s":"woof!","animalType":"Dog"}"""
```

## Similar projects

- [play-json-derived-codecs](https://github.com/julienrf/play-json-derived-codecs)
- [play-json-extensions](https://github.com/xdotai/play-json-extensions)
