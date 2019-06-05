package org.kodein.memory.concurent


class PhonyLock

actual typealias Lock = PhonyLock

actual inline fun <T> Lock.withLock(action: () -> T): T = action()

actual typealias RWLock = PhonyLock

actual inline fun <T> RWLock.write(action: () -> T): T = action()

actual inline fun <T> RWLock.read(action: () -> T): T = action()
