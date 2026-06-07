package com.resolution

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.resolution.models.commons.EventType.EventType
import com.resolution.models.commons.Source.Source
import com.resolution.models.internal.{Interaction, Metrics, User}

import java.util.UUID

object DBActor {

  sealed trait Command

  final case class UserExist(userId: String, replyTo: ActorRef[Boolean]) extends Command
  final case class GetUser(userId: String, replyTo: ActorRef[User]) extends Command
  final case class GetInteraction(interactionId: UUID, replyTo: ActorRef[Interaction]) extends Command
  final case class GetInteractionFromUser(userId: String, replyTo: ActorRef[Set[UUID]]) extends Command
  final case class HasMetrics(userId: String, replyTo: ActorRef[Boolean]) extends Command
  final case class GetMetric(userId: String, replyTo: ActorRef[Metrics]) extends Command

  final case class AddUser(userId: String, parent: Option[String]) extends Command
  final case class AddParent(userId: String, parent: String) extends Command
  final case class GetParent(userId: String, replyTo: ActorRef[String]) extends Command
  final case class AddInteraction(interactionId: UUID, uids: Set[String], source: Source, event: EventType) extends Command
  final case class AddUserInteraction(userId: String, interactionId: UUID) extends Command
  final case class UpdateUsersInteraction(users: Set[String], interactionId: UUID) extends Command
  final case class CreateMetric(userId: String, source: Source, eventType: EventType) extends Command
  final case class DeleteMetric(userId: String) extends Command
  final case class DeleteUser(userId: String) extends Command
  final case class CalculateMetrics() extends Command
  final case class FindInteractionsFromConnectedUsers(userIds: Set[String], replyTo: ActorRef[Set[UUID]]) extends Command
  final case class UpdateMetrics(userId: String, source: Source, eventType: EventType) extends Command
  final case class MergeMetrics(referenceId: String, currentId: String) extends Command

  def apply(): Behavior[Command] = active(DB())

  private def active(state: DB): Behavior[Command] = Behaviors.receiveMessage {

    case UserExist(userId, replyTo) =>
      replyTo ! state.userExist(userId)
      Behaviors.same

    case GetUser(userId, replyTo) =>
      replyTo ! state.getUser(userId)
      Behaviors.same

    case GetInteraction(interactionId, replyTo) =>
      replyTo ! state.getInteraction(interactionId)
      Behaviors.same

    case GetInteractionFromUser(userId, replyTo) =>
      replyTo ! state.getInteractionFromUser(userId)
      Behaviors.same

    case HasMetrics(userId, replyTo) =>
      replyTo ! state.hasMetrics(userId)
      Behaviors.same

    case GetMetric(userId, replyTo) =>
      replyTo ! state.getMetric(userId)
      Behaviors.same

    case AddUser(userId, parent) =>
      active(state.addUser(userId, parent))

    case AddParent(userId, parent) =>
      active(state.addParent(userId, parent))

    case GetParent(userId, replyTo) =>
      val (rootParentId, nextState) = state.getParent(userId)
      replyTo ! rootParentId
      Behaviors.same
      active(nextState)

    case AddInteraction(interactionId, uids, source, event) =>
      active(state.addInteraction(interactionId, uids, source, event))

    case AddUserInteraction(userId, interactionId) =>
      active(state.addUserInteraction(userId, interactionId))

    case UpdateUsersInteraction(users, interactionId) =>
      active(state.updateUsersInteraction(users, interactionId))

    case CreateMetric(userId, source, eventType) =>
      active(state.createMetric(userId, source, eventType))

    case DeleteMetric(userId) =>
      active(state.deleteMetric(userId))

    case DeleteUser(userId) =>
      active(state.deleteUser(userId))

    case CalculateMetrics() =>
      active(state.calculateMetrics())

    case FindInteractionsFromConnectedUsers(userIds, replyTo) =>
      replyTo ! state.findInteractionsFromConnectedUsers(userIds)
      Behaviors.same

    case UpdateMetrics(userId, source, eventType) =>
      active(state.updateMetrics(userId, source, eventType))

    case MergeMetrics(referenceId, currentId) =>
      active(state.mergeMetrics(referenceId, currentId))
}

}