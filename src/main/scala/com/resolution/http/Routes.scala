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
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Keep, Source}
import scala.concurrent.Promise

import scala.concurrent.duration.DurationInt

object Routes {

  private lazy val log = LoggerFactory.getLogger(getClass)

  def define(
              dbActor: ActorRef[DBActor.Command]
            )(implicit system: akka.actor.typed.ActorSystem[_]): Route = {

    import Directives._
    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
    import io.circe.generic.auto._

    implicit val ec: scala.concurrent.ExecutionContext = system.executionContext
    implicit val timeout: Timeout = 10.seconds

    // 👇 COLA SECUENCIAL: Fuerza a que todo se procese en estricto orden de llegada
    val queue = Source.queue[(DBActor.Command, Promise[DBActor.Confirmation])](
        bufferSize = 1000,
        overflowStrategy = OverflowStrategy.backpressure
      )
      .mapAsync(parallelism = 1) { case (command, promise) =>
        // Envía al actor y espera a que termine antes de procesar el siguiente elemento de la cola
        dbActor.ask[DBActor.Confirmation](ref =>
          command match {
            case c: DBActor.ProcessCollect => c.copy(replyTo = ref)
            case u: DBActor.ProcessUpdate => u.copy(replyTo = ref)
            case other => other
          }
        ).map(res => promise.success(res))
      }
      .toMat(akka.stream.scaladsl.Sink.ignore)(Keep.left)
      .run()

    path("ping") {
      get {
        complete(Confirmation.ok)
      }
    } ~ path("collect") {
      post {
        entity(as[CollectInteraction]) { interaction: CollectInteraction =>
          log.info(f"POST /collect - $interaction")

          // En lugar de hacer un 'ask' directo, encolamos la petición de forma segura
          val promise = Promise[DBActor.Confirmation]()
          queue.offer((DBActor.ProcessCollect(interaction, system.deadLetters), promise))

          onSuccess(promise.future) { _ =>
            complete(StatusCodes.OK)
          }
        }
      }
    } ~ path("update") {
      post {
        entity(as[UpdateInteraction]) { update: UpdateInteraction =>
          log.info(f"POST /update - $update")

          // Encolamos la petición para asegurar el orden cronológico
          val promise = Promise[DBActor.Confirmation]()
          queue.offer((DBActor.ProcessUpdate(update, system.deadLetters), promise))

          onSuccess(promise.future) { _ =>
            complete(StatusCodes.OK)
          }
        }
      }
    } ~ path("metrics") {
      get {
        log.info("GET /metrics")
        // 👇 Ahora consulta de forma segura al actor en lugar de leer una variable global
        val snapshotFuture = dbActor.ask(ref => DBActor.ProcessMetrics(ref))
        onSuccess(snapshotFuture) { snapshot =>
          complete(snapshot)
        }
      }
    }
  }
}
