package org.kodein.memory.io

import org.khronos.webgl.ArrayBuffer


public actual fun Allocation.Allocations.native(capacity: Int): Allocation = TypedArrayKBuffer(ArrayBuffer(capacity)).asManagedAllocation()
