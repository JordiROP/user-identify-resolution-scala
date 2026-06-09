package com.resolution

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.resolution.jobs.{CollectJob, MetricsJob, UpdateJob}
import com.resolution.models.input.{CollectInteraction, UpdateInteraction}
import com.resolution.models.output.MetricsResponse

object DBActor {
  @volatile var currentMetricsSnapshot: MetricsResponse = MetricsResponse(0, 0, 0)
  sealed trait Command

  final case class ProcessCollect(interaction: CollectInteraction) extends Command
  private final case class ProcessMetrics(replyTo: ActorRef[MetricsResponse]) extends Command
  final case class ProcessUpdate(interaction: UpdateInteraction) extends Command

  def apply(): Behavior[Command] = {
    val collectJob = new CollectJob()
    val metricsJob = new MetricsJob()
    val updateJob = new UpdateJob()

    active(DB(), collectJob, metricsJob, updateJob)
  }

  private def active(state: DB,
                     collectJob: CollectJob,
                     metricsJob: MetricsJob,
                     updateJob: UpdateJob): Behavior[Command] = Behaviors.receiveMessage {

    case ProcessCollect(interaction) =>
      val newState = collectJob.execute(interaction, state)
      currentMetricsSnapshot = metricsJob.getMetrics(newState)
      active(newState, collectJob, metricsJob, updateJob)

    case ProcessMetrics(replyTo) =>
      replyTo ! currentMetricsSnapshot
      Behaviors.same

    case ProcessUpdate(interaction) =>
      val newState = updateJob.execute(interaction, state)
      currentMetricsSnapshot = metricsJob.getMetrics(newState)
      active(newState, collectJob, metricsJob, updateJob)

}

}