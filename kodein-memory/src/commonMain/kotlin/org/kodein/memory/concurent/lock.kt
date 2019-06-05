package org.kodein.memory.concurent

expect class Lock()

expect inline fun <T> Lock.withLock(action: () -> T): T


expect class RWLock()

expect inline fun <T> RWLock.write(action: () -> T): T
expect inline fun <T> RWLock.read(action: () -> T): T

