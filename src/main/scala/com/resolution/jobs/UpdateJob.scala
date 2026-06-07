package com.resolution.jobs

import akka.actor.typed.ActorRef
import com.resolution.DBActor.Command

import scala.concurrent.ExecutionContext

class UpdateJob(dbActor: ActorRef[Command])(implicit ec: ExecutionContext, system: akka.actor.typed.ActorSystem[_]) {


}
