package org.ivan.experiments.coroutines

import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.coroutines.experimental.startCoroutine

class HttpHeadersReader {

  class UnexpectedHttpFormatException(s: String) : Throwable(s)

  fun startReading(reader: SuspendingFillableByteSource): List<String> {
    val headers = mutableListOf<String>()
    val ref: suspend HttpHeadersReader.() -> Unit = { readHeaders(reader, headers) }
    ref.startCoroutine(this, object : Continuation<Unit> {
      override val context = EmptyCoroutineContext

      override fun resume(value: Unit) {
      }

      override fun resumeWithException(exception: Throwable) = throw exception
    })
    return headers
  }

  private suspend fun readHeaders(reader: SuspendingFillableByteSource, headers: MutableList<String>) {
    var line: String
    line = reader.readRequiredLine() // read first header
    if (!line.isEmpty()) {
      headers.add(line)
    }
    while (true) { // read remaining headers and empty line after them
      line = reader.readRequiredLine()
      if (line.isEmpty()) break
      headers.add(line)
    }
  }

  private suspend fun SuspendingFillableByteSource.readRequiredLine(): String {
    return readLine() ?: throw UnexpectedHttpFormatException("Next line is expected")
  }

  private suspend fun SuspendingFillableByteSource.readLine(): String? {
    val sb = StringBuilder()
    while (true) {
      val c = read()
      if (c == '\n'.toInt()) {
        return sb.toString()
      }
      if (c == -1) {
        return if (sb.isNotEmpty()) sb.toString() else null
      }
      sb.append(c.toChar())
    }
  }
}

fun main(args: Array<String>) {
  val headersReader = HttpHeadersReader()
  val source = SuspendingFillableByteSource()
  val headers = headersReader.startReading(source)
  println(headers)
  source.addBytes("GET / HTTP/1.1\n".toByteArray())
  println(headers)
  source.addBytes("Host: ".toByteArray())
  println(headers)
  source.addBytes("example.org\n".toByteArray())
  println(headers)
  source.addBytes("Connection: close".toByteArray())
  println(headers)
  source.addBytes("\n".toByteArray())
  println(headers)
  source.addBytes("\n".toByteArray())
  println(headers)
}
