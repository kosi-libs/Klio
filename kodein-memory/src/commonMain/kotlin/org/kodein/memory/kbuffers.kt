package org.kodein.memory

fun KBuffer.limitHere() {
    limit = position
}
