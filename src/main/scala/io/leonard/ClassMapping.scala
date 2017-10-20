package io.leonard

import play.api.libs.json.Format

case class Mapping[A](
                       name: String,
                       format: Format[A]
                     )
