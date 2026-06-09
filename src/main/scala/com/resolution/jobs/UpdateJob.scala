package com.resolution.jobs

import com.resolution.DB
import com.resolution.models.input.UpdateInteraction
import com.resolution.models.internal.Interaction
import com.resolution.models.input.CollectInteraction

import java.util.UUID

import org.slf4j.LoggerFactory


class UpdateJob {
  private lazy val log = LoggerFactory.getLogger(getClass)

  def execute(interaction: UpdateInteraction, state: DB): DB = {
    val savedInteraction: Interaction = state.getInteraction(interaction.id)
    val savedUsers: Set[String] = savedInteraction.userIds
    val toUpdateUsers: Set[String] = interaction.userIds.toSet
    val toUpdateExisting: Set[String] = toUpdateUsers.filter(state.userExist)
    val toVisit: Set[String] = savedUsers.union(toUpdateExisting)
    val (toDelete: Set[String], recompute: Set[UUID]) = state.findInteractionsFromConnectedUsers(toVisit)
    val state1: DB = state.deleteUsers(toDelete)
    val state2: DB = state1.updateUsersInteraction(toUpdateUsers, interaction.id)
    log.info(recompute.toString())
    val collectJob: CollectJob = new CollectJob()
    val finalState = recompute.foldLeft(state2) { (accState, recomputeUUID) =>
      val recomputeInteraction: Interaction = state.getInteraction(recomputeUUID)
      collectJob.execute(
        new CollectInteraction(
          recomputeUUID,
          recomputeInteraction.source,
          recomputeInteraction.event,
          recomputeInteraction.userIds),
        accState,
        doMetrics = false)
    }
    finalState.calculateMetrics()
  }
}