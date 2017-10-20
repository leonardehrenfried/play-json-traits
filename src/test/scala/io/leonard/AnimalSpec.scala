package io.leonard

object AnimalSpec {
  sealed trait Animal
  case class Dog(s: String) extends Animal
  case class Cat(s: String) extends Animal
  case object Nessy         extends Animal

  val doggy = Dog("woof!")
  val kitty = Cat("Meow!")

}
