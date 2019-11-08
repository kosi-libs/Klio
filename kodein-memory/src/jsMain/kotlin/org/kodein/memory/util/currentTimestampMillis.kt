package org.kodein.memory.util

import kotlin.js.Date

actual fun currentTimestampMillis(): Long = Date.now().toLong()
