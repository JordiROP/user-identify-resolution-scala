package com.resolution

import com.resolution.models.input.{CollectInteraction, UpdateInteraction}
import com.resolution.models.output.MetricsResponse
import io.circe.parser._
import io.circe.syntax._
import zio._
import zio.http._
import zio.http.netty.NettyConfig


object HTTPServer extends ZIOAppDefault {
  def isValid(c: CollectInteraction): Boolean = {
    c.id != null &&
      c.userIds.nonEmpty &&
      (c.source.toString == "webpage" || c.source.toString == "appscreen") &&
      (c.event.toString == "display" || c.event.toString == "buy")
  }

  def routes(queue: Queue[Command]): Routes[Any, Response] =
    Routes(
      Method.POST / "collect" -> handler { (req: Request) =>
        (for {
          _ <- ZIO.logInfo(f"/POST - collect - ${req.body.asString}")
          bodyStr <- req.body.asString
          response <- io.circe.parser.decode[CollectInteraction](bodyStr) match {
            case Right(collect) if isValid(collect) =>
              for {
                p <- Promise.make[Nothing, Unit]
                _ <- queue.offer(RunCollect(collect, p))
                _ <- p.await
              } yield Response.ok
            case _ =>
              ZIO.succeed(Response.ok)
          }
        } yield response).catchAll { _ =>
          ZIO.succeed(Response.ok)
        }
      },
      Method.POST / "update" -> handler { (req: Request) =>
        (for {
          _ <- ZIO.logInfo(f"/POST - update - ${req.body.asString}")
          bodyStr <- req.body.asString
          response <- decode[UpdateInteraction](bodyStr) match {
            case Right(update) if update.id != null && update.userIds.nonEmpty =>
              for {
                p <- Promise.make[Nothing, Unit]
                _ <- queue.offer(RunUpdate(update, p))
                _ <- p.await
              } yield Response.ok
            case _ =>
              ZIO.succeed(Response.ok)
          }
        } yield response).catchAll { _ =>
          ZIO.succeed(Response.ok)
        }
      },
      Method.GET / "metrics" -> handler {
        for {
          _ <-    ZIO.logInfo(f"/GET - metrics")
          p       <- Promise.make[Nothing, MetricsResponse]
          _       <- queue.offer(GetMetrics(p))
          metrics <- p.await
          jsonStr  = metrics.asJson.noSpaces
        } yield Response.json(jsonStr)
      }
    )

  override val run: ZIO[Any, Any, Any] = for {
    stateRef <- Ref.make(DB())
    queue    <- Queue.bounded[Command](50000)
    _        <- Processor.startWorker(queue, stateRef).fork
    singleThreadedNetty = NettyConfig.default.maxThreads(1)
    _        <- ZIO.logInfo("Processing Engine online. Port 8080...")
    _        <- Server.serve(routes(queue)).provide(
      Server.customized,
      ZLayer.succeed(Server.Config.default.port(8080)),
      ZLayer.succeed(singleThreadedNetty))
  } yield ()
}