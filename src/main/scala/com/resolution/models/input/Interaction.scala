package com.resolution.models.input

import com.resolution.models.commons.Source.Source
import com.resolution.models.commons.InteractionType.InteractionType
import com.resolution.models.commons.{InteractionType, Source}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

import java.util.UUID

final case class Interaction(id: UUID,
                       source: Source,
                       event: InteractionType,
                       userIds: Set[String])

object Interaction {
  // define enumeration codecs for Interaction
  implicit val sourceDecoder: Decoder[Source.Value] =
    Decoder.decodeEnumeration(Source)
  implicit val sourceEncoder: Encoder[Source.Value] =
    Encoder.encodeEnumeration(Source)
  implicit val eventTypeDecoder: Decoder[InteractionType.Value] =
    Decoder.decodeEnumeration(InteractionType)
  implicit val eventTypeEncoder: Encoder[InteractionType.Value] =
    Encoder.encodeEnumeration(InteractionType)

  //see: https://circe.github.io/circe/codecs/semiauto-derivation.html
  implicit val eventDecoder: Decoder[Interaction] = deriveDecoder[Interaction]
  implicit val eventEncoder: Encoder[Interaction] = deriveEncoder[Interaction]
}

final case class UpdateInteraction(id: UUID, userIds: Seq[String])