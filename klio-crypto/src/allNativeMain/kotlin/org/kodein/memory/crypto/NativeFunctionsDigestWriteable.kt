package org.kodein.memory.crypto

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ptr


internal typealias NativeDigestInitFunction<C> = (context: CPointer<C>) -> Unit
internal typealias NativeDigestUpdateFunction<C> = (context: CPointer<C>, input: CPointer<*>, inputLength: Int) -> Unit
internal typealias NativeDigestFinalFunction<C> = (context: CPointer<C>, output: CPointer<*>) -> Unit
internal typealias NativeDigestCloseFunction<C> = (context: CPointer<C>) -> Unit

internal class NativeFunctionsDigestWriteable<C : CPointed>(
        override val digestSize: Int,
        private val ctx: C,
        private val init: NativeDigestInitFunction<C>,
        private val update: NativeDigestUpdateFunction<C>,
        private val final: NativeDigestFinalFunction<C>,
        private val close: NativeDigestCloseFunction<C>
) : NativeDigestWriteable() {

    init {
        init(ctx.ptr)
    }

    override fun doUpdate(dataPtr: CPointer<*>, dataLength: Int) {
        update(ctx.ptr, dataPtr, dataLength)
    }

    override fun doFinal(outputPtr: CPointer<*>) {
        final(ctx.ptr, outputPtr)
    }

    override fun doReset() {
        init(ctx.ptr)
    }

    override fun doClose() {
        close(ctx.ptr)
    }
}
