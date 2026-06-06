package com.resolution.models.internal

import com.resolution.models.commons.EventType
import com.resolution.models.commons.EventType.EventType
import com.resolution.models.commons.Source.Source

final case class Metrics(sources: Set[Source],
                         display: Int,
                         buy: Int) {
  def addSource(source: Source): Metrics = this.copy(sources = this.sources + source)
  def isBounced: Boolean = if (this.display == 1 && this.buy == 0) true else false
  def isCrossed: Boolean = if (this.sources.size == 2) true else false
  def addEvent(eventType: EventType): Metrics = {
    eventType match {
      case EventType.buy =>
        this.copy(buy = this.buy + 1)
      case EventType.display =>
        this.copy(display = this.display +1)
    }
  }

  def updateMetrics(source: Source, eventType: EventType): Metrics = {
    eventType match {
      case EventType.buy =>
        this.copy(buy = this.buy + 1, sources = this.sources + source)
      case EventType.display =>
        this.copy(display = this.display + 1, sources = this.sources + source)
    }
  }

  def mergeMetrics(other: Metrics): Metrics = {
    this.copy(
      sources = this.sources.union(other.sources),
      display = this.display + other.display,
      buy = this.buy + other.buy
    )
  }
}

object  Metrics {
  def apply(source: Source, eventType: EventType): Metrics = {
    val sources: Set[Source] = Set(source)
    val display: Int = if (eventType == EventType.display) 1 else 0
    val buy: Int = if (eventType == EventType.buy) 1 else 0
    Metrics(sources, display, buy)
  }
}
