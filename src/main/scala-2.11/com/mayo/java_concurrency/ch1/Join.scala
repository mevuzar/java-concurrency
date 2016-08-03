package com.mayo.java_concurrency.ch1

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * @author yoav @since 8/1/16.
 */
object Join extends App{
  val dataSourceLoader = new SleeperLoader(4)
  val dtThread = new Thread(dataSourceLoader)
  val connectionLoader = new SleeperLoader(6)
  val connectionsThread = new Thread(connectionLoader)

  dtThread.start
  connectionsThread.start

  dtThread.join
  connectionsThread.join

  println(s"End of program: ${ZonedDateTime.now}\n")
}


class SleeperLoader(time: Int) extends Runnable {
  override def run(): Unit = {
    println(s"Beginning data sources loading: ${ZonedDateTime.now}\n");
    try {
      TimeUnit.SECONDS.sleep(time);
    } catch {
      case e: InterruptedException => e.printStackTrace();
    }
    println(s"Data sources loading has finished: ${ZonedDateTime.now}\n")
  }
}