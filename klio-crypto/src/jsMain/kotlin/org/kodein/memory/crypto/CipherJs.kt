package org.kodein.memory.crypto

import org.khronos.webgl.Uint8Array
import org.kodein.memory.crypto.lib.browserify_aes.JsCipher
import org.kodein.memory.crypto.lib.browserify_aes.createCipheriv
import org.kodein.memory.crypto.lib.browserify_aes.createDecipheriv
import org.kodein.memory.crypto.lib.safe_buffer.Buffer
import org.kodein.memory.io.*


private class JsCipherWriteable(private val cipher: JsCipher, private val key: ArrayBufferMemory, private val output: Writeable) : CipherWriteable {

    private var bytesWritten = 0
    override val position: Int get() = bytesWritten

    override fun requestCanWrite(needed: Int) {}

    private val bytesBuffer = ArrayBufferMemory(8)

    override fun writeByte(value: Byte) {
        bytesBuffer.putByte(0, value)
        update(bytesBuffer.slice(0, 1).uint8Array)
    }

    override fun writeShort(value: Short) {
        bytesBuffer.putShort(0, value)
        update(bytesBuffer.slice(0, 2).uint8Array)
    }

    override fun writeInt(value: Int) {
        bytesBuffer.putInt(0, value)
        update(bytesBuffer.slice(0, 4).uint8Array)
    }

    override fun writeLong(value: Long) {
        bytesBuffer.putLong(0, value)
        update(bytesBuffer.slice(0, 8).uint8Array)
    }

    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
        val buffer = ArrayBufferMemory(length)
        buffer.putBytes(0, src)
        update(buffer.uint8Array)
    }

    override fun writeBytes(src: ReadMemory) {
        when (val srcMemory = src.internalMemory()) {
            is ArrayBufferMemory -> update(srcMemory.uint8Array)
            else -> {
                val buffer = ArrayBufferMemory(src.size)
                buffer.putBytes(0, src)
                update(buffer.uint8Array)
            }
        }
    }

    override fun writeBytes(src: Readable, length: Int) {
        when (src) {
            is MemoryReadable -> writeBytes(src.readSlice(length))
            else -> {
                val buffer = ArrayBufferMemory(length)
                buffer.putBytes(0, src, length)
                update(buffer.uint8Array)
            }
        }
    }

    private fun update(src: Uint8Array) {
        val ciphered = cipher.update(Buffer(src))
        bytesWritten += src.length
        if (ciphered.length > 0) output.writeBytes(ArrayBufferMemory(ciphered))
    }

    override fun flush() {}

    override fun close() {
        val ciphered = cipher.final()
        if (ciphered.length > 0) output.writeBytes(ArrayBufferMemory(ciphered))

        key.fill(0)
    }

}

public actual object AES128 {
    private fun getCipher(mode: CipherMode, key: ReadMemory, create: (String, Uint8Array, Uint8Array?) -> JsCipher, output: Writeable): JsCipherWriteable {
        require(key.size in arrayOf(16, 24, 32)) { "Key must be 16, 24 or 32 bytes (not ${key.size})." }

        val keyCopy = ArrayBufferMemory(key.size)
        keyCopy.putBytes(0, key)


        val modeName = when (mode) {
            is CipherMode.CBC -> "aes-${key.size * 8}-cbc"
            CipherMode.ECB -> "aes-${key.size * 8}-ecb"
        }

        val iv = when (mode) {
            is CipherMode.CBC -> {
                if (mode.iv != null) {
                    require(mode.iv.size == 16) { "IV must be 16 bytes." }
                    val iv = ArrayBufferMemory(16)
                    iv.putBytes(0, mode.iv)
                    iv.uint8Array
                } else {
                    null
                }
            }
            CipherMode.ECB -> null
        }

        return JsCipherWriteable(create(modeName, keyCopy.uint8Array, iv), keyCopy, output)
    }

    public actual fun encrypt(mode: CipherMode, key: ReadMemory, output: Writeable): CipherWriteable =
        getCipher(mode, key, ::createCipheriv, output)

    public actual fun decrypt(mode: CipherMode, key: ReadMemory, output: Writeable): CipherWriteable =
        getCipher(mode, key, ::createDecipheriv, output)
}
