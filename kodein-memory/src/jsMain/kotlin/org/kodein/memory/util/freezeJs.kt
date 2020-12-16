package org.kodein.memory.util


public actual fun <T : Any> T.freeze(): T = this

public actual fun <T : Any> T.ensureNeverFrozen() {}

public actual val <T : Any> T.isFrozen: Boolean get() = false
