package org.kodein.memory.concurent

import kotlin.concurrent.write as ktWrite
import kotlin.concurrent.read as ktRead
import kotlin.concurrent.withLock as ktWithLock


actual typealias Lock = java.util.concurrent.locks.ReentrantLock

actual inline fun <T> Lock.withLock(action: () -> T): T = ktWithLock(action)



actual typealias RWLock = java.util.concurrent.locks.ReentrantReadWriteLock

actual inline fun <T> RWLock.write(action: () -> T): T = ktWrite(action)

actual inline fun <T> RWLock.read(action: () -> T): T = ktRead(action)
