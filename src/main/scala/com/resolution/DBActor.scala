package com.resolution

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.resolution.models.internal.{Interaction, Metrics, User}

import java.util.UUID

object DBActor {

  sealed trait Command

  // Basic Writes example modify to match the db ones
  final case class SaveUser(user: User) extends Command

  // Basic Reads example modify to match the db ones
  final case class FindUser(userId: String, replyTo: ActorRef[Option[User]]) extends Command

  def apply(): Behavior[Command] = active(DB())

  private def active(state: DB): Behavior[Command] = Behaviors.receiveMessage {

    // Thread-Safe Writes (Updates state, loops with new snapshot)example modify to match the db ones
    case SaveUser(user) =>
      active(state.addUser(user))

      // Thread-Safe Reads (Safely exposes snapshots without mutating)example modify to match the db ones
    case FindUser(userId, replyTo) =>
      replyTo ! state.users.get(userId)
      Behaviors.same
}

}