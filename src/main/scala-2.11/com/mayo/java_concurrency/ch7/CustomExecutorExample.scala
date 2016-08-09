package com.mayo.java_concurrency
package ch7

import java.time.ZonedDateTime
import java.util
import java.util.concurrent.{BlockingQueue, Callable, ConcurrentHashMap, LinkedBlockingDeque, ThreadPoolExecutor, TimeUnit}

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionException

/**
 * @author yoav @since 8/8/16.
 */
object CustomExecutorExample extends App with LazyLogging {
  val myExecutor = new CustomExecutor(2, 4, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingDeque[Runnable]())

  val results = new util.ArrayList[java.util.concurrent.Future[String]]

  
  Range(0, 10) foreach { i =>
    val task = new SleepTwoSecondsTask()
    val result = myExecutor.submit(task)
    results.add(result)
  }

  Range(0, 5) foreach { i =>
    tryThreadCatchPrint {
      val result = results.get(i).get();
      logger.info(s"Main: Result for Task $i : $result")
    }
  }

  myExecutor.shutdown()

  Range(5, 10) foreach { i =>
    val result = results.get(i).get();
    logger.info(s"Main: Result for Task $i : $result")
  }
}

class CustomExecutor(val corePoolSize: Int,
                     val maxPoolSize: Int,
                     val keepAliveTime: Int,
                     val unit: TimeUnit,
                     workQueue: BlockingQueue[Runnable]) extends
ThreadPoolExecutor(
  corePoolSize,
  maxPoolSize,
  keepAliveTime,
  unit,
  workQueue) with LazyLogging {

  val startTimes: ConcurrentHashMap[String, ZonedDateTime] = new ConcurrentHashMap[String, ZonedDateTime]

  override def afterExecute(r: Runnable, t: Throwable): Unit = {
    val result = r.asInstanceOf[java.util.concurrent.Future[_]]
    try {
      logger.info("*********************************")
      logger.info("MyExecutor: A task is finishing.")
      logger.info(s"MyExecutor: Result: ${result.get()}")
      val startDate = startTimes.remove(String.valueOf(r.
        hashCode()))
      val finishDate = ZonedDateTime.now
      val diff = finishDate.toInstant.toEpochMilli - startDate.toInstant.toEpochMilli
      logger.info("MyExecutor: Duration: %d\n", diff)
      logger.info("*********************************")
    } catch {
      case e: InterruptedException => e.printStackTrace()
      case e: ExecutionException => e.printStackTrace()
    }
  }

  override def shutdownNow(): util.List[Runnable] = {
    logger.info(s"MyExecutor: Going to immediately shutdown.")
    logger.info(s"MyExecutor: Executed tasks: $getCompletedTaskCount")
    logger.info(s"MyExecutor: Running tasks: $getActiveCount")
    logger.info(s"MyExecutor: Pending tasks: ${getQueue().size()}")
    return super.shutdownNow()
  }

  override def shutdown(): Unit = {
    logger.info(s"MyExecutor: Going to shutdown.\n")
    logger.info(s"MyExecutor: Executed tasks: $getCompletedTaskCount")
    logger.info(s"MyExecutor: Running tasks: $getActiveCount")
    logger.info(s"MyExecutor: Pending tasks: ${getQueue().size()}")
    super.shutdown()
  }

  override def beforeExecute(t: Thread, r: Runnable): Unit = {
    logger.info(s"MyExecutor: A task is beginning: ${t.getName()} : ${r.hashCode()}")
    startTimes.put(String.valueOf(r.hashCode()), ZonedDateTime.now)
  }
}

class SleepTwoSecondsTask extends Callable[String] {
  override def call(): String = {
    TimeUnit.SECONDS.sleep(2)
    ZonedDateTime.now.toString
  }
}