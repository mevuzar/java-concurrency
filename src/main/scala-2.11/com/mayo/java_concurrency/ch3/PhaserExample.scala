package com.mayo.java_concurrency
package ch3

import java.io.File
import java.time.{Instant, ZoneId, ZonedDateTime}
import java.util
import java.util.concurrent.Phaser
import java.util.function.Predicate

/**
 * @author yoav @since 8/3/16.
 */
object PhaserExample extends App {
  val phaser = new Phaser(3)

  val usrSearch = new FileSearch("/usr/", ".log", phaser)
  val privateSearch = new FileSearch("/private/", ".log", phaser)
  val optSearch = new FileSearch("/opt/", ".log", phaser)

  val usrThread = new Thread(usrSearch, "usrSearch")
  val privateThread = new Thread(privateSearch, "privateSearch")
  val optThread = new Thread(optSearch, "optSearch")

  usrThread.start
  privateThread.start
  optThread.start
}

class FileSearch(val initialPath: String, ext: String, phaser: Phaser) extends Runnable {
  val results = new util.ArrayList[File]

  override def run(): Unit = {
    val initFile = new File(initialPath)

    if(initFile.isDirectory){
      directoryPocess(initFile)
      if(checkResults){
        filterResults
        if(checkResults){
          showInfo
          phaser.arriveAndDeregister
          println(s"${currentThreadName}: Work completed.\n")
        }
      }
    }
  }

  def directoryPocess(file: File): Unit = {
    require(file != null, "wtf dude?!?!?!?!?")
    try {
      val fileList = file.listFiles
      val files = if(fileList == null) List.empty[File] else fileList.toList
      files.foreach { f =>
        if (f.isDirectory)
          directoryPocess(f)
        else
          fileProcess(f)
      }
    }
    catch {
      case e: NullPointerException =>{
        val i = 0
        e.printStackTrace
      }
    }
  }

  def fileProcess(file: File): Unit = {
    if (file.getName.endsWith(ext)) {
      results.add(file)
    }
  }

  def filterResults: Unit = {
    results.removeIf(new Predicate[File] {
      override def test(t: File): Boolean = {
        ZonedDateTime.
          ofInstant(Instant.ofEpochMilli(t.lastModified), ZoneId.systemDefault).
          isBefore(ZonedDateTime.now.minusYears(2))
      }
    })
  }

  def checkResults: Boolean = {
    if (results.isEmpty) {
      println(s"Thread ${currentThreadName}: no results at phase: ${phaser.getPhase}")
      println(s"${currentThreadName}: Phase : ${phaser.getPhase}} End.\n")
      phaser.arriveAndDeregister
      false
    } else {
      println(s"Thread ${currentThreadName}: ${results.size} results at phase: ${phaser.getPhase}")
      phaser.arriveAndAwaitAdvance
      true
    }
  }

  def showInfo: Unit = {
    results.
      toArray.
      toList.
      map(_.asInstanceOf[File]).
      foreach { o => println(s"Thread: ${
      Thread.currentThread.
        getName
    }, path: ${o.getAbsolutePath}")
    }

    phaser.arriveAndAwaitAdvance
  }
}
