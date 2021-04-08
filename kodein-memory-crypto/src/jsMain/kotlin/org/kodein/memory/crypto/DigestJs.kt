package org.kodein.memory.crypto

import org.khronos.webgl.*
import org.kodein.memory.crypto.lib.JsDigest
import org.kodein.memory.crypto.lib.create_hmac.createHmac
import org.kodein.memory.crypto.lib.safe_buffer.Buffer
import org.kodein.memory.crypto.lib.sha_js.*
import org.kodein.memory.io.*


internal class JsDigestWriteable(override val digestSize: Int, private val onClose: () -> Unit = {}, private val newSha: () -> JsDigest) : DigestWriteable {

    private var jsSha = newSha()

    private var bytesWritten = 0
    override val position: Int get() = bytesWritten

    private val buffer = ArrayBufferMemory(DataView(ArrayBuffer(8)))

    override fun requestCanWrite(needed: Int) {}

    override fun writeByte(value: Byte) {
        buffer.putByte(0, value)
        writeBytes(buffer.uint8Array.subarray(0, 1))
    }

    override fun writeShort(value: Short) {
        buffer.putShort(0, value)
        writeBytes(buffer.uint8Array.subarray(0, 2))
    }

    override fun writeInt(value: Int) {
        buffer.putInt(0, value)
        writeBytes(buffer.uint8Array.subarray(0, 4))
    }

    override fun writeLong(value: Long) {
        buffer.putLong(0, value)
        writeBytes(buffer.uint8Array.subarray(0, 8))
    }

    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
        require(srcOffset >= 0) { "srcOffset: $srcOffset < 0" }
        require(length >= 0) { "length: $length < 0" }
        require(srcOffset + length <= src.size) { "srcOffset: $srcOffset + length: $length > src.size: ${src.size}" }

        val array = Uint8Array(length)
        for (i in 0 until length) {
            array[i] = src[srcOffset + i]
        }
        writeBytes(array)
    }

    override fun writeBytes(src: ReadMemory) {
        when (val srcMemory = src.internalMemory()) {
            is ArrayBufferMemory -> writeBytes(srcMemory.uint8Array)
            else -> {
                val array = Uint8Array(src.size)
                for (i in 0 until src.size) {
                    array[i] = src[i]
                }
                writeBytes(array)
            }
        }
    }

    override fun writeBytes(src: Readable, length: Int) {
        when (src) {
            is MemoryReadable -> writeBytes(src.readMemory(length))
            else -> {
                val array = Uint8Array(length)
                for (i in 0 until length) {
                    array[i] = src.readByte()
                }
                writeBytes(array)
            }
        }
    }

    private fun writeBytes(src: Uint8Array) {
        jsSha.update(src)
    }

    override fun flush() {}

    override fun close() {}

    override fun digestInto(dst: ByteArray, dstOffset: Int) {
        val array = jsSha.digest()
        for (i in 0 until digestSize) {
            dst[dstOffset + i] = array[i]
        }
    }

    override fun digestInto(dst: Memory) {
        val array = jsSha.digest()
        dst.putBytes(0, ArrayBufferMemory(array))
    }

    override fun digestInto(dst: Writeable) {
        val array = jsSha.digest()
        dst.writeBytes(ArrayBufferMemory(array))
    }

    override fun reset() {
        jsSha = newSha()
    }

}

private val DigestAlgorithm.jsAlgorithmDigestSize get() = when (this) {
    DigestAlgorithm.SHA1 -> 20
    DigestAlgorithm.SHA224 -> 28
    DigestAlgorithm.SHA256 -> 32
    DigestAlgorithm.SHA384 -> 48
    DigestAlgorithm.SHA512 -> 64
}

internal val DigestAlgorithm.jsAlgorithmName get() = when (this) {
    DigestAlgorithm.SHA1 -> "sha1"
    DigestAlgorithm.SHA224 -> "sha224"
    DigestAlgorithm.SHA256 -> "sha256"
    DigestAlgorithm.SHA384 -> "sha384"
    DigestAlgorithm.SHA512 -> "sha512"
}

public actual fun DigestWriteable.Companion.newInstance(algorithm: DigestAlgorithm): DigestWriteable =
    JsDigestWriteable(algorithm.jsAlgorithmDigestSize) { shajs(algorithm.jsAlgorithmName) }

public actual fun DigestWriteable.Companion.newHmacInstance(algorithm: DigestAlgorithm, key: ReadMemory): DigestWriteable {
    val keyCopy = ArrayBufferMemory.copyOf(key)

    val onClose = { keyCopy.fill(0) }

    return JsDigestWriteable(algorithm.jsAlgorithmDigestSize, onClose) { createHmac(algorithm.jsAlgorithmName, Buffer(keyCopy.uint8Array)) }
}
