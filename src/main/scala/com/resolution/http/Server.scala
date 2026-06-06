package com.resolution.http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.resolution.DBActor
import com.resolution.actor.DatabaseActor
import com.resolution.service.AnalyticsService
import com.typesafe.config.Config

import java.net.InetSocketAddress
import scala.util.{Failure, Success}

object Server {

  def main(args: Array[String]): Unit = {
    start()
  }

  def start(): Unit = {
    // 1. We create the ROOT behavior of our typed system.
    // This acts as the structural supervisor of our entire application lifecycle.
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      implicit val system: ActorSystem[_] = context.system
      implicit val ec = context.executionContext

      val log = context.log
      val config: Config = system.settings.config

      // 2. SPAWN THE AGENT-IN-CHARGE (Created EXACTLY ONCE at boot)
      val dbActorRef = context.spawn(DBActor(), "global-database-gatekeeper")

      // 3. INJECT into your Business Services
      // DEFINE HERE UPDATE COLLECT METRICS

      // 4. INJECT the service directly into your Routes generator
      // Your HTTP routes can now freely trigger background reads and writes!
      // val applicationRoutes = Routes.define(analyticsService)

      val interface: String = config.getString("web-app.http.interface")
      val port: Int = config.getInt("web-app.http.port")

      // 5. Bind the HTTP server to the network using the typed system context
      // (Note: Akka HTTP requires a classic system under the hood, which context.system.classicSystem handles automatically)
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

    // Initialize the main Typed Actor System container using our root layout
    ActorSystem[Nothing](rootBehavior, "AnalyticsPlatformSystem")
  }
}