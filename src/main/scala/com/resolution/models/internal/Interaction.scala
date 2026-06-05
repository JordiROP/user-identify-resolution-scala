package com.resolution.models.internal

import com.resolution.models.commons.Source.Source
import com.resolution.models.commons.EventType.EventType

final case class Interaction (
                               userIds: Set[String],
                               source: Source,
                               event: EventType
                             )

