package com.resolution.models.internal

import com.resolution.models.commons.EventType
import com.resolution.models.commons.EventType.EventType
import com.resolution.models.commons.Source.Source

final case class Metrics(sources: Set[Source],
                         display: Int,
                         buy: Int)

object  Metrics {
  def apply(source: Source, eventType: EventType): Metrics = {
    val sources: Set[Source] = Set(source)
    val display: Int = if (eventType == EventType.display) 1 else 0
    val buy: Int = if (eventType == EventType.buy) 1 else 0
    Metrics(sources, display, buy)
  }
}
