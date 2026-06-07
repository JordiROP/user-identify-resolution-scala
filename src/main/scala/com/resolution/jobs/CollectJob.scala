package com.resolution.jobs

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import com.resolution.DBActor
import com.resolution.DBActor._
import com.resolution.models.commons.EventType.EventType
import com.resolution.models.commons.Source.Source
import com.resolution.models.input.Interaction

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

class CollectJob(dbActor: ActorRef[Command])(implicit ec: ExecutionContext, system: akka.actor.typed.ActorSystem[Nothing]) {

  implicit val timeout: Timeout = Timeout(5.seconds)

  private def resolveUser(currRef: String): Future[String] = {
    dbActor.ask[Boolean](ref => DBActor.UserExist(currRef, ref)).flatMap { userExists =>
      if (userExists) {
        dbActor.ask[String](ref => DBActor.GetParent(currRef, ref))
      } else {
        dbActor.tell(DBActor.AddUser(currRef, None))
        Future.successful(currRef)
      }
    }
  }

  private def createOrUpdateMetric(currParent: String, source: Source, eventType: EventType): Future[Unit] = {
    dbActor.ask[Boolean](ref => DBActor.HasMetrics(currParent, ref)).flatMap { userExists =>
      if (!userExists) {
        dbActor.tell(DBActor.CreateMetric(currParent, source, eventType))
        Future.successful()
      } else {
        dbActor.tell(DBActor.UpdateMetrics(currParent, source, eventType))
        Future.successful()
      }
    }
  }

  def processCollect(interaction: Interaction): Unit = {
    dbActor.tell(AddInteraction(interaction.id, interaction.userIds, interaction.source, interaction.event))

    val currRef: String = interaction.userIds.head

    for {
      parent <- resolveUser(currRef)
      _ = dbActor.tell(AddUserInteraction(currRef, interaction.id))
      _ = createOrUpdateMetric(parent, interaction.source, interaction.event)

      _ <- Future.sequence(interaction.userIds.tail.map { nextUser =>
        dbActor.tell(DBActor.AddUserInteraction(nextUser, interaction.id))
        resolveUser(nextUser).flatMap{ currParent =>

          if (currParent != parent) {
            dbActor.tell(DBActor.AddParent(currParent, parent))
            dbActor.ask[Boolean](ref => DBActor.HasMetrics(currParent, ref)).flatMap{ hasMetrics =>
              if (hasMetrics) {
                dbActor.tell(DBActor.MergeMetrics(parent, currParent))
                dbActor.tell(DBActor.DeleteMetric(currParent))
                Future.successful()
              } else {
                Future.successful()
              }
            }
          } else {
            Future.successful()
          }
        }
      })
      _ = dbActor.tell(DBActor.CalculateMetrics())
    } yield()
  }
}
