package com.mayo

/**
 * @author yoav @since 8/3/16.
 */
package object java_concurrency {
  def currentThreadName = Thread.currentThread.getName
  def tryThreadCatchPrint(f:  => Unit) = {
    try {
      f
    }
    catch {
      case e: InterruptedException => e.printStackTrace()
    }
  }
}
