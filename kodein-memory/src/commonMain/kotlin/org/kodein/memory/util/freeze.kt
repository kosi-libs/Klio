package org.kodein.memory.util


public expect fun <T : Any> T.freeze(): T

public expect fun <T : Any> T.ensureNeverFrozen()

public expect val <T : Any> T.isFrozen: Boolean
