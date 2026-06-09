package com.resolution.http

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import com.resolution.DBActor
import com.resolution.models.input.{CollectInteraction, UpdateInteraction}
import com.resolution.models.output.Confirmation
import org.slf4j.LoggerFactory
import akka.util.Timeout

import scala.concurrent.duration.DurationInt

object Routes_OLD {

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
    implicit val timeout: Timeout = 10.seconds

    path("ping") {
      get {
        complete(Confirmation.ok)
      }
    } ~ path("collect") {
      post {
        entity(as[CollectInteraction]) { interaction: CollectInteraction =>
          log.info(f"POST /collect - $interaction")
          val confirmation = dbActor.ask(ref => DBActor.ProcessCollect(interaction, ref))
          onSuccess(confirmation) { _ =>
            complete(StatusCodes.OK)
          }
        }
      }
    } ~ path("update") {
      post {
        entity(as[UpdateInteraction]) { update: UpdateInteraction =>
          log.info(f"POST /update - $update")
          val confirmation = dbActor.ask(ref => DBActor.ProcessUpdate(update, ref))
          onSuccess(confirmation) { _ =>
            complete(StatusCodes.OK)
          }
        }
      }
    } ~ path("metrics") {
      get {
        log.info("GET /metrics")
        val metrics = dbActor.ask(ref => DBActor.ProcessMetrics(ref))
        onSuccess(metrics) { _ =>
          complete(metrics)
        }
      }
    }
  }
}
