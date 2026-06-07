package com.resolution.http

import akka.http.scaladsl.server.{Directives, Route}
import com.resolution.jobs.{CollectJob, UpdateJob, MetricsJob}
import com.resolution.models.input.{Interaction, UpdateInteraction}
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
              collectJob: CollectJob,
              updateJob: UpdateJob,
              metricsJob: MetricsJob
            ): Route = {
    // TODO DEFINE SERVICES INSIDE THE HEADER WHEN CREATED
    import Directives._
    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
    import io.circe.generic.auto._

    // ping endpoint
    path("ping") {
      get {
        complete(Confirmation.ok)
      }
    } ~ path("collect") {
      post {
        entity(as[Interaction]) { interaction: Interaction =>
          log.info(f"POST /collect - $interaction")
          // TODO: your code goes here: data collection
          complete(Confirmation.ok)
        }
      }
      // id update endpoint
    } ~ path("update") {
      post {
        entity(as[UpdateInteraction]) { update: UpdateInteraction =>
          log.info(f"POST /update - $update")
          // TODO: your code goes here: ids update
          complete(Confirmation.ok)
        }
      }
      // metrics endpoint
    } ~ path("metrics") {
      get {
        log.info(f"GET /metrics")
        // TODO: your code goes here: metrics calculation

        complete(Confirmation.ok)
      }
    }
  }
}
