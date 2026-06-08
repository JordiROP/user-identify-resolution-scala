package com.resolution.http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.resolution.DBActor
import com.typesafe.config.Config

import java.net.InetSocketAddress
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Server {
  def start(): Unit = {

    val rootBehavior = Behaviors.setup[Nothing] { context =>
      implicit val system: ActorSystem[_] = context.system
      implicit val ec: ExecutionContextExecutor = context.executionContext

      val log = context.log
      val config: Config = system.settings.config

      val dbActorRef = context.spawn(DBActor(), "global-database-gatekeeper")

      val applicationRoutes = Routes.define(dbActorRef)

      val interface: String = config.getString("web-app.http.interface")
      val port: Int = config.getInt("web-app.http.port")

      Http()(system.classicSystem)
        .newServerAt(interface, port)
        .bindFlow(applicationRoutes)
        .onComplete {
          case Success(binding) =>
            val address: InetSocketAddress = binding.localAddress
            log.info(s"Server online at http://${address.getHostString}:${address.getPort}/")
          case Failure(e) =>
            log.error("Failed to bind HTTP endpoint, terminating system", e)
            system.terminate()
        }
      Behaviors.empty
    }
    ActorSystem[Nothing](rootBehavior, "AnalyticsPlatformSystem")
  }
}