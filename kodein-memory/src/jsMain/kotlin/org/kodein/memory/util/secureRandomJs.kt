package org.kodein.memory.util

import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Int32Array
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import kotlin.random.Random
import kotlin.reflect.typeOf


private external interface BrowserCrypto {
    fun getRandomValues(array: ArrayBufferView)
}

private external interface NodeCrypto {
    fun randomFillSync(array: ArrayBufferView)
}

private class SecureRandomJs(private val fill: (ArrayBufferView) -> Unit) : Random() {

    override fun nextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray {
        val size = toIndex - fromIndex

        val mem = Int8Array(size)
        fill(mem)
        for (i in 0 until size) array[fromIndex + i] = mem[i]

        return array
    }

    override fun nextInt(): Int {
        val mem = Int32Array(1)
        fill(mem)
        return mem[0]
    }

    override fun nextBits(bitCount: Int): Int = nextInt().takeUpperBits(bitCount)

    companion object {
        @Suppress("UnsafeCastFromDynamic")
        internal val instance: SecureRandomJs by lazy {
            when {
                jsTypeOf(js("window")) == "object" && js("window") != null -> {
                    val crypto = js("window.crypto").unsafeCast<BrowserCrypto?>() ?: error("This browser does not support crypto.")

                    SecureRandomJs { crypto.getRandomValues(it) }
                }
                jsTypeOf(js("require")) == "function" -> {
                    val crypto = js("require('crypto')").unsafeCast<NodeCrypto?>() ?: error("Could not load module crypto.")

                    SecureRandomJs { crypto.randomFillSync(it) }
                }
                else -> error("Could not determine platform: both window & require are undefined.")
            }
        }
    }
}

public actual fun Random.Default.secure(): Random = SecureRandomJs.instance
