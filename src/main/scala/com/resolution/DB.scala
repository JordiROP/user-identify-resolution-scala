package com.resolution

import com.resolution.models.commons.EventType
import com.resolution.models.commons.Source.Source
import com.resolution.models.commons.EventType.EventType
import com.resolution.models.internal.{Interaction, Metrics, User}

import java.util.UUID
import scala.annotation.tailrec

final case class DB(
                   interactions: Map[UUID, Interaction] = Map.empty,
                   users: Map[String, User] = Map.empty,
                   userInteractions: Map[String, Set[UUID]] = Map.empty,
                   metrics: Map[String, Metrics] = Map.empty,
                   uniqueUsers: Int = 0,
                   bouncedUsers: Int = 0,
                   xDeviceUsers: Int = 0
                   ) {
  def userExist(userId: String): Boolean = users.contains(userId)
  def getUser(userId: String): User = users(userId)
  def getInteraction(interactionId: UUID): Interaction = interactions(interactionId)
  def getInteractionFromUser(userId: String): Set[UUID] = userInteractions(userId)
  def hasMetrics(userId: String): Boolean = metrics.contains(userId)
  def getMetric(userId: String): Metrics = metrics(userId)

  def addUser(userId: String, parent: Option[String]): DB = {
    val newUser: User = User(userId, parent.getOrElse(userId))
    this.copy(users = this.users + (userId -> newUser))
  }

  def addParent(userId: String, parent: String): DB = {
    val newUser = users(userId).copy(parent = parent)
    this.copy(users = this.users + (userId -> newUser))
  }

  def getParent(userId: String): (String, DB) = {
    @tailrec
    def findRoot(current: String): String = {
      val parent: String = users(current).parent
      if (parent == current) current
      else findRoot(parent)
    }
    val parent = findRoot(userId)

    val user = users(userId)
    if (user.parent == parent) {
      (parent, this)
    } else {
      val updatedUser: User = User(userId, parent)
      (parent, this.copy(users = this.users + (userId -> updatedUser)))
    }
  }

  def addInteraction(interactionId: UUID, uids: Set[String], source: Source, event: EventType):DB = {
    val interaction: Interaction = Interaction(uids, source, event)
    this.copy(interactions = this.interactions + (interactionId -> interaction))
  }

  def addUserInteraction(userId: String, interactionId: UUID): DB = {
    val interactions = this.userInteractions(userId) + interactionId
    this.copy(userInteractions = this.userInteractions + (userId -> interactions))
  }

  def updateUsersInteraction(users: Set[String], interactionId: UUID): DB = {
    val updatedInteraction = getInteraction(interactionId).copy(userIds = users)
    this.copy(interactions = this.interactions + (interactionId -> updatedInteraction))
  }

  def createMetric(userId: String, source: Source, eventType: EventType): DB = {
    val metrics: Metrics = Metrics.apply(source, eventType)
    this.copy(metrics = this.metrics + (userId -> metrics))
  }

  def deleteMetric(userId: String): DB = {
    this.copy(metrics = this.metrics - userId)
  }

  def deleteUser(userId: String): DB = {
    if (this.hasMetrics(userId)) {
      this.copy(
        users = this.users - userId,
        userInteractions = this.userInteractions - userId,
        metrics = this.metrics - userId)
    } else {
      this.copy(
        users = this.users - userId,
        userInteractions = this.userInteractions - userId)
    }
  }

  def calculateMetrics(): DB = {
    val counters: (Int, Int, Int) = (0, 0, 0)

    val (unique, bounced, cross) = this.metrics.values.foldLeft(counters){
      case ((currUnique, currBounced, currCross), metric) =>
        val uniqueInc = currUnique + 1
        val bouncedInc = if (metric.isBounced) currBounced + 1 else currBounced
        val crossInc = if (metric.isCrossed) currCross +1 else currCross

        (uniqueInc, bouncedInc, crossInc)
    }
    this.copy(
      uniqueUsers = unique,
      bouncedUsers = bounced,
      xDeviceUsers = cross
    )
  }

  def findInteractionsFromConnectedUsers(userIds: Set[String]): Set[UUID] = {
    @tailrec
    def traverse(toVisitUsers: List[String], visitedUsers: Set[String], visitedInteractions: Set[UUID]): Set[UUID] = {
      toVisitUsers match {
        case Nil =>
          visitedInteractions
        case currentId :: restUserIds =>
          if (visitedUsers.contains(currentId)) {
            traverse(restUserIds, visitedUsers + currentId, visitedInteractions)
          } else {
            val newInteractions = this.userInteractions(currentId)
            val interactionUsers = newInteractions.flatMap(interactionId => {this.interactions(interactionId).userIds})
            val newUsers = interactionUsers -- visitedUsers - currentId

            traverse(restUserIds ++ newUsers.toList, visitedUsers + currentId, visitedInteractions ++ newInteractions)
          }
      }
    }

    traverse(userIds.toList, Set.empty, Set.empty)
  }

  def updateMetrics(userId: String, source: Source, eventType: EventType): DB = {
    val metric = getMetric(userId)
    val updatedMetrics = metric.updateMetrics(source, eventType)
    this.copy(metrics = this.metrics + (userId -> updatedMetrics))
  }

  def mergeMetrics(referentId: String, currentId: String): DB = {
    val referentMetric: Metrics = getMetric(referentId)
    val currentMetrics: Metrics = getMetric(currentId)
    val mergedMetrics = referentMetric.mergeMetrics(currentMetrics)
    this.copy(metrics = this.metrics + (referentId -> mergedMetrics))
  }

}
