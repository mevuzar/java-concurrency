package com.mayo.java_concurrency.ch5


import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.forkjoin.{ForkJoinPool, ForkJoinTask, RecursiveAction}

/**
 * @author yoav @since 8/7/16.
 */
object ForkJoinSimpleExample extends App with LazyLogging {

  val products = ProductListGenerator(10000).map(p => new AtomicReference[Product](p))
  val task = new Task(products, 0, products.size, 0.20)
  val pool = new ForkJoinPool()
  pool.execute(task)
  pool.shutdown()

  while (task.isDone == false) {
    logger.info(s"Main: Thread Count: ${
      pool.
        getActiveThreadCount()
    }")
    logger.info(s"Main: Thread Steal: ${
      pool.
        getStealCount()
    }")
    logger.info(s"Main: Parallelism: ${
      pool.
        getParallelism()
    }")

    try {
      TimeUnit.MILLISECONDS.sleep(5)
    } catch {
      case e: InterruptedException => e.printStackTrace()
    }
  }

  logger.info(s"products: \n${products.take(10).map(_.get).mkString("\n")}")

}

class Task(val products: List[AtomicReference[Product]], private val first: Int, private val last: Int, private val increment: Double) extends
RecursiveAction with
LazyLogging {
  private final val serialVersionUID = 1L

  override def compute(): Unit = {
    if (last - first < 10) {
      updatePrices()
    } else {
      val middle = (last + first) / 2;
      logger.info(s"Task: Pending tasks: ${ForkJoinTask.getQueuedTaskCount()} \n")
      val t1 = new Task(products, first, middle + 1, increment)
      val t2 = new Task(products, middle + 1, last, increment)
      ForkJoinTask.invokeAll(t1, t2)
    }
  }

  //  def invokeAll(tasks: Task*): Unit ={
  //    this.get
  //    tasks.foreach(_.invoke)
  //  }

  def updatePrices(): Unit = Range(first, last) map { i =>
    val newPrice = products(i).get.price + increment
    products(i).set(products(i).get.copy(price = newPrice))
  }


}

case class Product(name: String, price: Double)

object ProductListGenerator {
  def apply(size: Int): List[Product] = {
    Range(1, size).map { i =>
      Product(s"product#$i", 1.0)
    }.toList
  }
}
