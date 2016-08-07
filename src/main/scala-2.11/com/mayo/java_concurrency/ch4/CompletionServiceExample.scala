package com.mayo.java_concurrency
package ch4

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{Callable, CompletionService, ExecutionException, ExecutorCompletionService, Executors, TimeUnit}

import com.typesafe.scalalogging.LazyLogging

import scala.util.Try

/**
 * @author yoav @since 8/4/16.
 */
object CompletionServiceExample extends App with LazyLogging {
  val executorService = Executors.newCachedThreadPool
  val service = new ExecutorCompletionService[String](executorService)

  val faceRequest = new ReportRequest("Face", service)
  val onlineRequest = new ReportRequest("Online", service)

  val faceThread = new Thread(faceRequest)
  val onlineThread = new Thread(onlineRequest)

  val processor = new ReportProcessor(service)
  val senderThread = new Thread(processor)

  faceThread.start()
  onlineThread.start()
  senderThread.start()

  Try {
    logger.info("Starting, waiting for report generators")

    faceThread.join()
    onlineThread.join()
  }

  logger.info("Main: Shutting down the executor.")

  executorService.shutdown()

  executorService.awaitTermination(1, TimeUnit.DAYS)
}

class ReportGenerator(val sender: String, val title: String) extends Callable[String] with LazyLogging {
  override def call(): String = {
    try {
      val duration = (Math.random * 10).toLong
      logger.info(s"${sender}_$title: ReportGenerator: Generating report:  during $duration seconds")
      TimeUnit.SECONDS.sleep(duration)
    }
    catch {
      case e: InterruptedException => e.printStackTrace()
    }

    sender + ": " + title
  }
}

class ReportRequest(val name: String, val service: CompletionService[String]) extends Runnable {
  override def run(): Unit = {
    val reportGenerator = new ReportGenerator(name, "Report")
    service.submit(reportGenerator)
  }
}

class ReportProcessor(val service: CompletionService[String]) extends Runnable with LazyLogging {
  private val end = new AtomicBoolean(false)

  override def run(): Unit = {
    while (!end.get) {
      try {
        val result = service.poll(20, TimeUnit.SECONDS)
        if (result != null) {
          val report = result.get();
          logger.info(s"ReportReceiver: Report Received: $report")
        }

      }
      catch {
        //case e:(InterruptedException | ExecutionException) => e.printStackTrace
        case any => any.printStackTrace()
      }

      logger.info(s"ReportSender: End")
    }
  }

  def setEnd(end: Boolean): Unit = {
    this.end.set(end)
  }
}
