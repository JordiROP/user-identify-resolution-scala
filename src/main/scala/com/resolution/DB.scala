package com.resolution

import com.contentsquare.model.Metrics
import com.resolution.models.internal.{Interaction, User}

import java.util.UUID

final case class DB(
                   interactions: Map[UUID, Interaction] = Map.empty,
                   users: Map[String, User] = Map.empty,
                   userInteractions: Map[String, Set[UUID]] = Map.empty,
                   metrics: Map[String, Metrics] = Map.empty,
                   uniqueUsers: Int = 0,
                   bouncedUsers: Int = 0,
                   xDeviceUsers: Int = 0
                   ) {}
