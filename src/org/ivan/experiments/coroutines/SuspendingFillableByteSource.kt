package org.ivan.experiments.coroutines

import java.util.*
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine


class SuspendingFillableByteSource {
  private val bytes = LinkedList<ByteArray>()
  private var readerIndex = 0
  private var end = false
  var continuation: Continuation<Int>? = null

  private fun isReadable() = bytes.isNotEmpty() && readerIndex < bytes.first.size

  suspend fun read(): Int {
    if (isReadable()) {
      return read0()
    }
    if (end) {
      return -1
    }
    return suspendCoroutine { cont ->
      continuation = cont
    }
  }

  private fun read0(): Int {
    val chunk = bytes.first
    val ret = chunk[readerIndex++].toInt()
    if (readerIndex == chunk.size) {
      bytes.removeFirst()
      readerIndex = 0
    }
    return ret
  }

  fun addBytes(b: ByteArray) {
    if (b.isNotEmpty()) {
      bytes.add(b)
      continuation?.resume(read0())
    }
  }

  fun end() {
    end = true
    continuation?.resume(-1)
  }
}