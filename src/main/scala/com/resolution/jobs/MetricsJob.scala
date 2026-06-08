package com.resolution.jobs

import com.resolution.DB
import com.resolution.models.output.MetricsResponse

class MetricsJob {

  def getMetrics(state: DB): MetricsResponse = {
      state.getMetrics
  }

}
