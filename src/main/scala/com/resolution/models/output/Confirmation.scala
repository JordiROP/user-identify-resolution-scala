package com.resolution.models.output


final case class Confirmation(status: String)

object Confirmation {
  val ok: Confirmation = Confirmation("OK")
}