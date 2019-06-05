package org.kodein.memory.concurent

expect class AtomicInteger(initial: Int = 0) {

    fun incrementAndGet(): Int

    fun set(value: Int)

    fun get(): Int
}
