package com.resolution.jobs

import com.resolution.DB
import com.resolution.models.input.CollectInteraction

class CollectJob {

  private def getParent(currRef: String, state: DB): (String, DB) = {
    val (parent, state2) = if (state.userExist(currRef)) {
      state.getParent(currRef)
    } else {
      (currRef, state.addUser(currRef, None))
    }
    (parent, state2)
  }

  private def processFirstParent(interaction: CollectInteraction, state: DB): (String, DB) = {
    val state1 = state.addInteraction(interaction.id, interaction.userIds, interaction.source, interaction.event)

    val currRef = interaction.userIds.head

    val (parent, state2) = getParent(currRef, state1)

    val state3 = state2.addUserInteraction(currRef, interaction.id)

    val state4 = if (state3.hasMetrics(parent)) {
      state3.updateMetrics(parent, interaction.source, interaction.event)
    } else {
      state3.createMetric(parent, interaction.source, interaction.event)
    }
    (parent, state4)
  }

  private def processRestUsers(parent: String, interaction: CollectInteraction, state: DB): DB = {
    val finalState = interaction.userIds.tail.foldLeft(state) { (accState, nextUser) =>
      val accState2 = accState.addUserInteraction(nextUser, interaction.id)
      val (currParent, accState3) = getParent(nextUser, accState2)

      if (currParent != parent) {
        val accState4 = accState3.addParent(currParent, parent)
        if (accState4.hasMetrics(currParent)) {
          accState4.mergeMetrics(parent, currParent).deleteMetric(currParent)
        } else {
          accState4
        }
      } else {
        accState3
      }
    }
    finalState
  }

  def execute(interaction: CollectInteraction, state: DB, doMetrics: Boolean = true): DB = {
    val (parent: String, nextState: DB) = processFirstParent(interaction, state)
    val finalState: DB = processRestUsers(parent, interaction ,nextState)

    if (doMetrics){
      finalState.calculateMetrics()
    } else {
      finalState
    }
  }
}