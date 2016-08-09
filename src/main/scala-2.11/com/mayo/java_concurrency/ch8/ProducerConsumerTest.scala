package com.mayo.java_concurrency
package ch8

import java.util.concurrent.LinkedTransferQueue

import com.typesafe.scalalogging.LazyLogging
import edu.umd.cs.mtc.{TestFramework, MultithreadedTestCase}
import junit.framework.AssertionFailedError

/**
 * @author yoav @since 8/9/16.
 */

object Main extends App {
  val test = new ProducerConsumerTest
  System.out.printf("Main: Starting the test\n")
  TestFramework.runOnce(test);
  System.out.printf("Main: The test has finished\n")
}

class ProducerConsumerTest extends MultithreadedTestCase with LazyLogging {
  private var queue: LinkedTransferQueue[String] = null

  override def initialize(): Unit = {
    super.initialize()
    queue = new LinkedTransferQueue[String]
    System.out.printf("Test: The test has been initialized\n")
  }

  def thread1() {
    val ret = queue.take()
    System.out.printf("Thread 1: %s\n", ret)
  }

  def thread2() {
    waitForTick(1)
    val ret = queue.take()
    System.out.printf("Thread 2: %s\n", ret)
  }

  def thread3() {
    waitForTick(1)
    waitForTick(2)
    queue.put("Event 1")
    queue.put("Event 2")
    System.out.printf("Thread 3: Inserted two elements\n")
  }

  override def finish() {
    super.finish()
    System.out.printf("Test: End\n")
    assertEquals(true, queue.size() == 0)
    System.out.printf("Test: Result: The queue is empty\n")
  }

  def failNotEquals(message: String, expected: AnyRef, actual: AnyRef) {
    fail(format(message, expected, actual))
  }

  def fail(message: String) {
    throw new AssertionFailedError(message)
  }

  def assertEquals (expected: Boolean, actual: Boolean) {
    assertEquals(null, expected.asInstanceOf[AnyRef], actual.asInstanceOf[AnyRef])
  }

  def assertEquals(message: String, expected: AnyRef, actual: AnyRef) {
    if (expected == null && actual == null) return
    if (expected != null && (expected == actual)) return
    failNotEquals(message, expected, actual)
  }

  def format(message: String, expected: AnyRef, actual: AnyRef): String = {
    var formatted: String = ""
    if (message != null) formatted = message + " "
    formatted + "expected:<" + expected + "> but was:<" + actual + ">"
  }
}

