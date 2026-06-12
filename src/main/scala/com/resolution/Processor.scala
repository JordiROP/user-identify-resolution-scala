package com.resolution

import zio._
import com.resolution.jobs.{CollectJob, UpdateJob, MetricsJob}

object Processor {
  private val collectJob = new CollectJob()
  private val updateJob = new UpdateJob()
  private val metricsJob = new MetricsJob()

  def startWorker(queue: Queue[Command], stateRef: Ref[DB]): UIO[Nothing] = {
    queue.take.flatMap {
      case RunCollect(interaction, p) =>
        for {
          _ <- ZIO.logInfo(s"/Collect - Unqueued: ${interaction.id}")
          _ <- stateRef.update { currentDb =>
            if (currentDb.interactions.contains(interaction.id)) currentDb
            else collectJob.execute(interaction, currentDb)
          }
          _ <- p.succeed(())
        } yield ()

      case RunUpdate(update, p) =>
        for {
          _ <- ZIO.logInfo(s"/Update - Unqueued: ${update.id}")
          _ <- stateRef.update { currentDb =>
            if (!currentDb.interactions.contains(update.id)) currentDb
            else updateJob.execute(update, currentDb)
          }
          _ <- p.succeed(())
        } yield ()

      case GetMetrics(p) =>
        for {
          _ <- ZIO.logInfo(s"/Metrics - Unqueued")
          currentDb <- stateRef.get
          _         <- p.succeed(metricsJob.getMetrics(currentDb))
        } yield ()
    }.forever
  }
}