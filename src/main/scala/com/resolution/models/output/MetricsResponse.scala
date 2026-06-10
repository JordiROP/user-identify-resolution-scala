package com.resolution.models.output

import com.resolution.models.commons.{EventType, Source}
import com.resolution.models.input.UpdateInteraction
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class MetricsResponse (uniqueUsers: Int, bouncedUsers: Int, crossDeviceUsers: Int)

object MetricsResponse {
  implicit val sourceDecoder: Decoder[Source.Value] =
    Decoder.decodeEnumeration(Source)
  implicit val sourceEncoder: Encoder[Source.Value] =
    Encoder.encodeEnumeration(Source)
  implicit val eventTypeDecoder: Decoder[EventType.Value] =
    Decoder.decodeEnumeration(EventType)
  implicit val eventTypeEncoder: Encoder[EventType.Value] =
    Encoder.encodeEnumeration(EventType)

  implicit val eventDecoder: Decoder[MetricsResponse] = deriveDecoder[MetricsResponse]
  implicit val eventEncoder: Encoder[MetricsResponse] = deriveEncoder[MetricsResponse]
}