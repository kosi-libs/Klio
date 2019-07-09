package org.kodein.memory

import org.khronos.webgl.ArrayBuffer


actual fun Allocation.Allocations.native(capacity: Int): Allocation = TypedArrayKBuffer(ArrayBuffer(capacity)).asManagedAllocation()
