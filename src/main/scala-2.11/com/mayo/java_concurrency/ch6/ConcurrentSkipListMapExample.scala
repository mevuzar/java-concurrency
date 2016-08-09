package com.mayo.java_concurrency
package ch6

import java.util.Map.Entry
import java.util.concurrent.ConcurrentSkipListMap

import com.typesafe.scalalogging.LazyLogging

/**
 * @author yoav @since 8/8/16.
 */
object ConcurrentSkipListMapExample extends App with LazyLogging{
  val map = new ConcurrentSkipListMap[String, Contact]
  val threads = Range('A', 'Z') map { letter=>
    val task = new Task(map, letter.toChar.toString)
    val thread = new Thread(task)
    thread.start
    thread
  }

  threads.foreach(thread => tryThreadCatchPrint(thread.join))

  logger.info(s"Main: Size of the map: ${map.size}")


  printElement(map.firstEntry())

  printElement(map.lastEntry())

  logger.info("Main: Submap from A1996 to B1002")

  val submap = map.subMap("A1996", "B1002")

  var element: Entry[String, Contact] = null
  do {
    element = submap.pollFirstEntry()
    if (element!=null) {
      val contact=element.getValue()
      printElement(element)
    }
  } while (element!=null)


  def printElement(element: Entry[String, Contact]): Unit = {
    val contact = element.getValue()
    logger.info(s"Main: Entry: ${contact.name}: ${contact.phone}")
  }
}

case class Contact(name: String, phone: String)

class Task(val map: ConcurrentSkipListMap[String, Contact], val id: String) extends Runnable {
  override def run(): Unit = {
    Range(0, 1000) foreach{ i =>
    val contact = Contact(id, String.valueOf(i+1000))
      map.put(id+contact.phone, contact)
    }
  }
}