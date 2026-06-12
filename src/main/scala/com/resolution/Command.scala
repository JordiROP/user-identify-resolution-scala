package com.resolution

import com.resolution.models.input.{CollectInteraction, UpdateInteraction}
import com.resolution.models.output.MetricsResponse
import zio.Promise

sealed trait Command

case class RunCollect(
                       interaction: CollectInteraction,
                       p: Promise[Nothing, Unit]
                     ) extends Command

case class RunUpdate(
                      interaction: UpdateInteraction,
                      p: Promise[Nothing, Unit]
                    ) extends Command

case class GetMetrics(
                       p: Promise[Nothing, MetricsResponse]
                     ) extends Command