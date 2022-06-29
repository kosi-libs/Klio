package org.kodein.memory.util

import kotlin.native.concurrent.freeze as nativeFreeze
import kotlin.native.concurrent.ensureNeverFrozen as nativeEnsureNeverFrozen
import kotlin.native.concurrent.isFrozen as nativeIsFrozen


public actual fun <T : Any> T.freeze(): T = nativeFreeze()

public actual fun <T : Any> T.ensureNeverFrozen(): Unit = nativeEnsureNeverFrozen()

public actual val <T : Any> T.isFrozen: Boolean get() = nativeIsFrozen
