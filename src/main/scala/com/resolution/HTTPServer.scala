package com.resolution

import com.resolution.models.input.{CollectInteraction, UpdateInteraction}
import com.resolution.models.output.MetricsResponse
import io.circe.syntax._
import zio._
import zio.http._

object HTTPServer extends ZIOAppDefault {

  private def isValid(c: CollectInteraction): Boolean = {
    c.id != null &&
      c.userIds.nonEmpty &&
      (c.source.toString == "webpage" || c.source.toString == "appscreen") &&
      (c.event.toString == "display" || c.event.toString == "buy")
  }

  private def routes(queue: Queue[Command], sem: Semaphore): HttpApp[Any] =
    Routes(
      Method.POST / "collect" -> handler { (req: Request) =>
        sem.withPermit {
          for {
            bodyStr <- req.body.asString.orDie
            _ <- ZIO.logInfo(s"Received /POST collect - : $bodyStr")
            res = io.circe.parser.decode[CollectInteraction](bodyStr) match {
              case Right(collect) if isValid(collect) =>
                for {
                  p <- Promise.make[Nothing, Unit]
                  _ <- queue.offer(RunCollect(collect, p))
                  _ <- p.await
                } yield Response.ok
              case _ =>
                ZIO.succeed(Response.badRequest("Invalid JSON"))
            }
            finalResponse <- res
          } yield finalResponse
        }
      },

      Method.POST / "update" -> handler { (req: Request) =>
        sem.withPermit {
          for {
            bodyStr <- req.body.asString.orDie
            _ <- ZIO.logInfo(s"Received /POST update - : $bodyStr")
            res = io.circe.parser.decode[UpdateInteraction](bodyStr) match {
              case Right(update) if update.id != null && update.userIds.nonEmpty =>
                for {
                  p <- Promise.make[Nothing, Unit]
                  _ <- queue.offer(RunUpdate(update, p))
                  _ <- p.await
                } yield Response.ok
              case _ =>
                ZIO.succeed(Response.badRequest("Invalid JSON"))
            }
            finalResponse <- res
          } yield finalResponse
        }
      },

      Method.GET / "metrics" -> handler {
        sem.withPermit {
          for {
            _ <- ZIO.logInfo(s"Received /GET metrics")
            p       <- Promise.make[Nothing, MetricsResponse]
            _       <- queue.offer(GetMetrics(p))
            metrics <- p.await
            jsonStr  = metrics.asJson.noSpaces
          } yield Response.json(jsonStr)
        }
      }
    ).toHttpApp

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    for {
      stateRef           <- Ref.make(DB())
      queue              <- Queue.unbounded[Command]
      admissionSemaphore <- Semaphore.make(permits = 1)
      _        <- Processor.startWorker(queue, stateRef).fork
      _        <- Server.serve(routes(queue, admissionSemaphore)).provide(Server.default)
    } yield ()
  }
}