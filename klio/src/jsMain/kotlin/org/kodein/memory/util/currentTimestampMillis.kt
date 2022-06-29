package org.kodein.memory.util

import kotlin.js.Date

public actual fun currentTimestampMillis(): Long = Date.now().toLong()
