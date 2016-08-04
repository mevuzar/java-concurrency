package com.mayo.java_concurrency.ch4

import java.util.concurrent.{ThreadPoolExecutor, Executors}

/**
 * Created by Owner on 8/4/2016.
 */
object ExecutorExample {
  val executorService = Executors.newCachedThreadPool.asInstanceOf[ThreadPoolExecutor]


}
