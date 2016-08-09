package com.mayo.java_concurrency
package ch6

import java.util.concurrent.ConcurrentLinkedDeque

import com.typesafe.scalalogging.LazyLogging

/**
 * @author yoav @since 8/8/16.
 */
object ConcurrentCollectionsSimpleExample extends App with LazyLogging {

  val list = new ConcurrentLinkedDeque[String]()
  val addThreads = Range(0, 100) map { i =>
    val task = new AddTask(list)
    val thread = new Thread(task)
    thread.start()
    thread
  }

  logger.info(s"Main: ${addThreads.length} AddTask threads have been launched");

  addThreads.foreach(thread => tryThreadCatchPrint(() => thread.join))

  logger.info(s"size of list: ${list.size()}")

  val pollThreads = Range(0, 100) map { i =>
    val task = new PollTask(list)
    val thread = new Thread(task)
    thread.start()
    thread
  }

  logger.info(s"Main: ${pollThreads.length} PollTask threads have been launched");

  pollThreads.foreach(thread => tryThreadCatchPrint(() => thread.join))

  logger.info(s"size of list: ${list.size()}")
}

class AddTask(val list: ConcurrentLinkedDeque[String]) extends Runnable {
  override def run(): Unit = {
    Range(0, 10000) foreach { i =>
      list.add(s"$currentThreadName: Element: $i")
    }
  }
}


class PollTask(val list: ConcurrentLinkedDeque[String]) extends Runnable {
  override def run(): Unit = {
    Range(0, 5000) foreach { i =>
      list.pollFirst()
      list.pollLast()
    }
  }
}
