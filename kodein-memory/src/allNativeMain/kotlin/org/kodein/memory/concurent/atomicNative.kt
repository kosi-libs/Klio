package org.kodein.memory.concurent

actual class AtomicInteger actual constructor(initial: Int) {

    private var value = initial

    actual fun incrementAndGet(): Int = ++value

    actual fun decrementAndGet(): Int = --value

    actual fun get() = value

    actual fun set(value: Int) { this.value = value }

}
