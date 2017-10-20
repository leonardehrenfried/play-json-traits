package io.leonard

import play.api.libs.json.Format

case class ClassMapping[A](
  name: String,
  format: Format[A]
)
