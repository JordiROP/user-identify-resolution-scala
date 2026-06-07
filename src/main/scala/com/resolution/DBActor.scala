package com.resolution

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.resolution.models.commons.EventType.EventType
import com.resolution.models.commons.Source.Source
import com.resolution.models.internal.{Interaction, Metrics, User}

import java.util.UUID

object DBActor {

  sealed trait Command

  final case class userExist(userId: String, replyTo: ActorRef[Boolean]) extends Command
  final case class getUser(userId: String, replyTo: ActorRef[User]) extends Command
  final case class getInteraction(interactionId: UUID, replyTo: ActorRef[Interaction]) extends Command
  final case class getInteractionFromUser(userId: String, replyTo: ActorRef[Set[UUID]]) extends Command
  final case class hasMetrics(userId: String, replyTo: ActorRef[Boolean]) extends Command
  final case class getMetric(userId: String, replyTo: ActorRef[Metrics]) extends Command

  final case class addUser(userId: String, parent: Option[String]) extends Command
  final case class addParent(userId: String, parent: String) extends Command
  final case class getParent(userId: String, replyTo: ActorRef[String]) extends Command
  final case class addInteraction(interactionId: UUID, uids: Set[String], source: Source, event: EventType) extends Command
  final case class addUserInteraction(userId: String, interactionId: UUID) extends Command
  final case class updateUsersInteraction(users: Set[String], interactionId: UUID) extends Command
  final case class createMetric(userId: String, source: Source, eventType: EventType) extends Command
  final case class deleteMetric(userId: String) extends Command
  final case class deleteUser(userId: String) extends Command
  final case class calculateMetrics() extends Command
  final case class findInteractionsFromConnectedUsers(userIds: Set[String], replyTo: ActorRef[Set[UUID]]) extends Command

  def apply(): Behavior[Command] = active(DB())

  private def active(state: DB): Behavior[Command] = Behaviors.receiveMessage {

    case userExist(userId, replyTo) =>
      replyTo ! state.userExist(userId)
      Behaviors.same

    case getUser(userId, replyTo) =>
      replyTo ! state.getUser(userId)
      Behaviors.same

    case getInteraction(interactionId, replyTo) =>
      replyTo ! state.getInteraction(interactionId)
      Behaviors.same

    case getInteractionFromUser(userId, replyTo) =>
      replyTo ! state.getInteractionFromUser(userId)
      Behaviors.same

    case hasMetrics(userId, replyTo) =>
      replyTo ! state.hasMetrics(userId)
      Behaviors.same

    case getMetric(userId, replyTo) =>
      replyTo ! state.getMetric(userId)
      Behaviors.same

    case addUser(userId, parent) =>
      active(state.addUser(userId, parent))

    case addParent(userId, parent) =>
      active(state.addParent(userId, parent))

    case getParent(userId, replyTo) =>
      val (rootParentId, nextState) = state.getParent(userId)
      replyTo ! rootParentId
      Behaviors.same
      active(nextState)

    case addInteraction(interactionId, uids, source, event) =>
      active(state.addInteraction(interactionId, uids, source, event))

    case addUserInteraction(userId, interactionId) =>
      active(state.addUserInteraction(userId, interactionId))

    case updateUsersInteraction(users, interactionId) =>
      active(state.updateUsersInteraction(users, interactionId))

    case createMetric(userId, source, eventType) =>
      active(state.createMetric(userId, source, eventType))

    case deleteMetric(userId) =>
      active(state.deleteMetric(userId))

    case deleteUser(userId) =>
      active(state.deleteUser(userId))

    case calculateMetrics() =>
      active(state.calculateMetrics())

    case findInteractionsFromConnectedUsers(userIds, replyTo) =>
      replyTo ! state.findInteractionsFromConnectedUsers(userIds)
      Behaviors.same
}

}