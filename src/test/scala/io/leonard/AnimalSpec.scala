package io.leonard

object AnimalSpec {
  sealed trait Animal
  sealed trait Mammal extends Animal
  case class Dog(s: String) extends Mammal
  case class Cat(s: String) extends Mammal
  case object Nessy         extends Animal

  val doggy = Dog("woof!")
  val kitty = Cat("Meow!")

}
