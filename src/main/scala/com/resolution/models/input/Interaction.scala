package com.resolution.models.input

import com.resolution.models.commons.Source.Source
import com.resolution.models.commons.EventType.EventType
import com.resolution.models.commons.{EventType, Source}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

import java.util.UUID

final case class Interaction(id: UUID,
                             source: Source,
                             event: EventType,
                             userIds: Set[String])

object Interaction {
  implicit val sourceDecoder: Decoder[Source.Value] =
    Decoder.decodeEnumeration(Source)
  implicit val sourceEncoder: Encoder[Source.Value] =
    Encoder.encodeEnumeration(Source)
  implicit val eventTypeDecoder: Decoder[EventType.Value] =
    Decoder.decodeEnumeration(EventType)
  implicit val eventTypeEncoder: Encoder[EventType.Value] =
    Encoder.encodeEnumeration(EventType)

  implicit val eventDecoder: Decoder[Interaction] = deriveDecoder[Interaction]
  implicit val eventEncoder: Encoder[Interaction] = deriveEncoder[Interaction]
}

final case class UpdateInteraction(id: UUID, userIds: Seq[String])