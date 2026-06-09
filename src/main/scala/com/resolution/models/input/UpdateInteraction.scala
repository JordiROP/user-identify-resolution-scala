package com.resolution.models.input

import com.resolution.models.commons.{EventType, Source}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.util.UUID

final case class UpdateInteraction(id: UUID, userIds: Seq[String])

object UpdateInteraction {
  implicit val sourceDecoder: Decoder[Source.Value] =
    Decoder.decodeEnumeration(Source)
  implicit val sourceEncoder: Encoder[Source.Value] =
    Encoder.encodeEnumeration(Source)
  implicit val eventTypeDecoder: Decoder[EventType.Value] =
    Decoder.decodeEnumeration(EventType)
  implicit val eventTypeEncoder: Encoder[EventType.Value] =
    Encoder.encodeEnumeration(EventType)

  implicit val eventDecoder: Decoder[UpdateInteraction] = deriveDecoder[UpdateInteraction]
  implicit val eventEncoder: Encoder[UpdateInteraction] = deriveEncoder[UpdateInteraction]
}