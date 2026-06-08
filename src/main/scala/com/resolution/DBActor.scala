package com.resolution

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.resolution.jobs.{CollectJob, MetricsJob}
import com.resolution.models.input.Interaction
import com.resolution.models.output.MetricsResponse

object DBActor {
  @volatile var currentMetricsSnapshot: MetricsResponse = MetricsResponse(0, 0, 0)
  sealed trait Command

  final case class ProcessCollect(interaction: Interaction) extends Command
  final case class ProcessMetrics(replyTo: ActorRef[MetricsResponse]) extends Command

  def apply(): Behavior[Command] = {
    val collectJob = new CollectJob()
    val metricsJob = new MetricsJob()
    active(DB(), collectJob, metricsJob)
  }


  private def active(state: DB,
                     collectJob: CollectJob,
                     metricsJob: MetricsJob): Behavior[Command] = Behaviors.receiveMessage {

    case ProcessCollect(interaction) =>
      val newState = collectJob.execute(interaction, state)
      currentMetricsSnapshot = metricsJob.getMetrics(newState)
      active(newState, collectJob, metricsJob)

    case ProcessMetrics(replyTo) =>
      replyTo ! currentMetricsSnapshot
      Behaviors.same

}

}