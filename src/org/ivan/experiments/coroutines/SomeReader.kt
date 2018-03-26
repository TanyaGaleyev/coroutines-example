package org.ivan.experiments.coroutines

import java.io.ByteArrayOutputStream
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.coroutines.experimental.startCoroutine


class SomeReader {
  val source = SuspendingFillableByteSource()
  private val fRead: suspend () -> Int = { source.read() }
  private val buf = arrayListOf<Int>()
  private var readSomeBytes = false
  private val cont = object : Continuation<Int> {
    override val context = EmptyCoroutineContext

    override fun resume(value: Int) {
      buf.add(value)
      readSomeBytes = true
    }

    override fun resumeWithException(exception: Throwable) = throw exception
  }

  fun readAvailable(): ByteArray {
    do {
      readSomeBytes = false
      fRead.startCoroutine(cont)
    } while (readSomeBytes)
    val baos = ByteArrayOutputStream()
    buf.forEach(baos::write)
    buf.clear()
    return baos.toByteArray()
  }
}

fun main(args: Array<String>) {
  println("Coroutine demo")
  val reader = SomeReader()
  reader.readAvailable().printHex()
  reader.source.addBytes(byteArrayOf(1, 2, 3))
  reader.readAvailable().printHex()
  reader.source.addBytes(byteArrayOf(1))
  reader.readAvailable().printHex()
}

private fun ByteArray.printHex() {
  val sb = StringBuilder()
  forEach {
    sb.append(Integer.toHexString(0xFF and it.toInt()))
  }
  println(sb.toString())
}