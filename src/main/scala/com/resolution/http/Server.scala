package com.resolution.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http

import java.net.InetSocketAddress
import scala.util.{Success, Failure}
import com.typesafe.config.Config

object Server {
  implicit private lazy val system: ActorSystem = ActorSystem("scala-template")
  private lazy val log: LoggingAdapter = system.log
  private lazy val config: Config = system.settings.config

  def start(): Unit = {
    import system.dispatcher
    // get web server interface and port from configuration (application.conf)
    val interface: String = config.getString("web-app.http.interface")
    val port: Int = config.getInt("web-app.http.port")

    // bind web server to the specified network address
    Http()
      .newServerAt(interface, port)
      // bind to defined route
      .bindFlow(Routes.define)
      // manage completion and failure
      .onComplete {
        case Success(binding) =>
          val address: InetSocketAddress = binding.localAddress
          log.info(
            s"Server online at http://${address.getHostString}:${address.getPort}/"
          )
        case Failure(e) =>
          log.error("Failed to bind HTTP endpoint, terminating system", e)
          system.terminate()
      }
  }
}
