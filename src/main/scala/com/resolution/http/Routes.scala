package com.resolution.http

import akka.actor.typed.ActorRef
import akka.http.scaladsl.server.{Directives, Route}
import com.resolution.DBActor
import com.resolution.jobs.MetricsJob
import com.resolution.models.input.{CollectInteraction, UpdateInteraction}
import com.resolution.models.output.Confirmation
import org.slf4j.LoggerFactory

object Routes {

  private lazy val log = LoggerFactory.getLogger(getClass)

  /**
    * This is the main entry point of the web application
    *
    * @param sys Akka Actor System
    * @return Route definition for Web endpoints
    */
  def define(
              dbActor: ActorRef[DBActor.Command]
            )(implicit system: akka.actor.typed.ActorSystem[_]): Route = {

    import Directives._
    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
    import io.circe.generic.auto._

    implicit val ec: scala.concurrent.ExecutionContext = system.executionContext
    path("ping") {
      get {
        complete(Confirmation.ok)
      }
    } ~ path("collect") {
      post {
        entity(as[CollectInteraction]) { interaction: CollectInteraction =>
          log.info(f"POST /collect - $interaction")
          dbActor.tell(DBActor.ProcessCollect(interaction))
          complete(Confirmation.ok)
        }
      }
    } ~ path("update") {
      post {
        entity(as[UpdateInteraction]) { update: UpdateInteraction =>
          log.info(f"POST /update - $update")
          dbActor.tell(DBActor.ProcessUpdate(update))
          complete(Confirmation.ok)
        }
      }
    } ~ path("metrics") {
      get {
        val snapshot = com.resolution.DBActor.currentMetricsSnapshot
        complete(snapshot)
      }
    }
  }
}
