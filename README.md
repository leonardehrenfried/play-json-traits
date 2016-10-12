#play-json-traits

This package allows you read and write traits using `play-json`. It does this
by adding a discriminator property to the JSON which can be used to identify
the precise implementation when parsing the JSON back to Scala.

## Example

```scala
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
// reuturn

val kittyJson = """{"s":"Meow!","type":"Cat"}"""
animalFormat.writes(kitty).toString() === kittyJson

```