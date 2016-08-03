package com.mayo.java_concurrency.ch1

import java.time.ZonedDateTime
import java.util
import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.LazyLogging

/**
 * @author yoav @since 8/1/16.
 */
object DaemonThreads extends App {
  val deque = new util.ArrayDeque[Event]()

  val writer = new WriterTask(deque)
  Range(0, 3).foreach { i => {
    val thread = new Thread(writer)
    thread.start()
  }
  }

  val cleaner = new CleanerTask(deque)
  cleaner.start()


}

case class Event(time: ZonedDateTime, info: String)

class WriterTask(val q: util.Deque[Event]) extends Runnable with LazyLogging{

  override def run(): Unit = {
    Range(0, 9).foreach { i =>
      val event = Event(ZonedDateTime.now, s"The thread ${Thread.currentThread().getId()} has generated an event")
      q.addFirst(event)
      logger.info(s"added event: $event")
      try {
        TimeUnit.SECONDS.sleep(2)
      } catch {
        case e: InterruptedException => e.printStackTrace()
      }
    }
  }
}

class CleanerTask(val q: util.Deque[Event]) extends Thread with LazyLogging{
  setDaemon(true)

  override def run(): Unit = {
    while (true) {
      val dateTime = ZonedDateTime.now
      clean(dateTime)
    }
  }

  def clean(dateTime: ZonedDateTime): Unit = {
    try {
      val event = q.getLast
      val toDelete = q.toArray.toList.map(_.asInstanceOf[Event]).filter(e => dateTime.isAfter(e.time.plusSeconds(10)))
      toDelete.foreach(e => {
        logger.info(s"deleting $e, provided date: $dateTime")
        q.remove(e)
      })
    }
    catch {
      case a =>
    }

  }
}