package com.resolution

import com.resolution.http.Server
import org.slf4j.LoggerFactory

object Main {
  def main(args: Array[String]): Unit = {
    LoggerFactory.getLogger(this.getClass)
    Server.start()
  }
}