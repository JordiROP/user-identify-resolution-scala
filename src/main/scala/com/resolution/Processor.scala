package com.resolution

import zio._
import com.resolution.jobs.{CollectJob, UpdateJob, MetricsJob}

object Processor {

  private val collectJob = new CollectJob()
  private val updateJob = new UpdateJob()
  private val metricsJob = new MetricsJob()

  def startWorker(queue: Queue[Command], stateRef: Ref[DB]): UIO[Nothing] = {
    queue.take.flatMap {
      case RunCollect(interaction, promise) =>
        stateRef.update { currentDb =>
          if (currentDb.interactions.contains(interaction.id)) currentDb
          else collectJob.execute(interaction, currentDb)
        } *> promise.succeed(()).unit
      case RunUpdate(update, promise) =>
        stateRef.update { currentDb =>
          if (!currentDb.interactions.contains(update.id)) currentDb
          else updateJob.execute(update, currentDb)
        } *> promise.succeed(()).unit
      case GetMetrics(promise) =>
        stateRef.get.flatMap { currentDb =>
          promise.succeed(metricsJob.getMetrics(currentDb)).unit
        }
    }.forever
  }
}