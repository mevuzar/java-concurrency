package com.mayo.java_concurrency.ch2

import java.util
import java.util.Random
import java.util.concurrent.locks.ReentrantLock

import scala.collection.mutable.ArrayBuffer

/**
 * @author yoav @since 8/3/16.
 */
object LockConditions extends App {
  val fileMock = new FileMock(100, 10)
  val buffer = new Buffer(20)

  val producer = new Producer(fileMock, buffer)
  val producerThread = new Thread(producer)


  val consumers = new ArrayBuffer[Consumer](3)
  val threadConsumers = new ArrayBuffer[Thread](3)

  Range(0, 3).map { i =>
    consumers.append(new Consumer(buffer))
    threadConsumers.append(new Thread(consumers(i), "Consumer " + i))
  }

  producerThread.start();

  threadConsumers.foreach(_.start)
}

class FileMock(size: Int, length: Int) {
  private val content = new ArrayBuffer[String](size)
  val lines = Range(0, size).map { i =>
    val buffer = new StringBuilder
    buffer.appendAll(Range(0, length).map(j => (Math.random() * 255).toChar)).toString
  }
  content.insertAll(0, lines)

  private var index: Int = 0

  def hasMoreLines = index < content.length;

  def getLine: String = {
    if (this.hasMoreLines) {
      println("Mock: " + (content.length - index));
      val tmp = index
      index += 1
      content(tmp)
    }
    null
  }

}

class Buffer(val maxSize: Int) {
  private val buffer = new util.LinkedList[String]
  private val lock = new ReentrantLock()
  private val lines = lock.newCondition()
  private val space = lock.newCondition()
  private var pendingLines = true

  def insert(line: String) {
    lock.lock()
    try {
      while (buffer.size() == maxSize) {
        println("Buffer is full, awaiting")
        space.await()
      }
      println("Buffer has space, inserting line")
      buffer.offer(line)
      printf("%s: Inserted Line: %d\n", Thread.
        currentThread().getName, buffer.size)
      lines.signalAll()
    } catch {
      case e: InterruptedException => e.printStackTrace
    } finally {
      lock.unlock();
    }
  }

  def get: String = {
    lock.lock
    try {
      while (buffer.size == 0 && hasPendingLines) {
        lines.await()
      }
      if (hasPendingLines) {
        val line = buffer.poll();
        printf("%s: Line Readed: %d\n", Thread.
          currentThread().getName(), buffer.size())
        space.signalAll()
        line
      } else {
        null
      }
    } catch {
      case e: InterruptedException =>
        e.printStackTrace
        null
    } finally {
      lock.unlock
    }
  }

  def setPendingLines(pendingLines: Boolean) {
    this.pendingLines = pendingLines
  }

  def hasPendingLines: Boolean = {
    pendingLines || buffer.size > 0
  }
}

class Producer(val fileMock: FileMock, buffer: Buffer) extends Runnable {
  override def run(): Unit = {

    buffer.setPendingLines(true)
    while (fileMock.hasMoreLines) {
      val line = fileMock.getLine
      buffer.insert(line)
    }
    buffer.setPendingLines(false)
  }

}

class Consumer(val buffer: Buffer) extends Runnable {
  override def run(): Unit = {
    while (buffer.hasPendingLines) {
      val line = buffer.get
      processLine(line)
    }
  }

  def processLine(line: String) {
    try {
      val random = new Random();
      Thread.sleep(random.nextInt(100));
    } catch {
      case e: InterruptedException => e.printStackTrace();
    }
  }
}