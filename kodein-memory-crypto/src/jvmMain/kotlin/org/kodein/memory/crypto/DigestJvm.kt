package org.kodein.memory.crypto

import org.kodein.memory.io.*
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


internal abstract class AbstractJvmDigestWriteable : DigestWriteable {

    abstract fun updateByte(byte: Byte)
    abstract fun updateBytes(bytes: ByteArray, offset: Int, length: Int)
    abstract fun getDigest(): ByteArray

    private var bytesWritten: Int = 0

    override val position: Int get() = bytesWritten

    override fun requestCanWrite(needed: Int) {}

    override fun writeByte(value: Byte) {
        updateByte(value)
        bytesWritten += 1
    }

    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
        updateBytes(src, srcOffset, length)
        bytesWritten += length
    }

    override fun writeBytes(src: ReadMemory) {
        if (src is ByteArrayMemory) writeBytes(src.array, src.offset, src.size)
        else writeBytes(src.getBytes())
    }

    override fun writeBytes(src: Readable, length: Int) {
        if (src is MemoryReadable) writeBytes(src.readMemory(length))
        else writeBytesBuffered(src, length)
    }

    private inline fun <T> writeValue(size: Int, value: T, storeValue: (T, (Int, Byte) -> Unit) -> Unit) {
        storeValue(value) { _, b -> updateByte(b) }
        bytesWritten += size
    }

    override fun writeShort(value: Short) = writeValue(2, value, ::slowStoreShort)
    override fun writeInt(value: Int) = writeValue(4, value, ::slowStoreInt)
    override fun writeLong(value: Long) = writeValue(8, value, ::slowStoreLong)

    override fun flush() {}

    override fun digestInto(dst: Memory) {
        require(dst.size >= digestSize) { "Memory is too small" }
        when (dst) {
            is ByteArrayMemory -> digestInto(dst.array, dst.offset)
            else -> dst.putBytes(0, getDigest(), 0, digestSize)
        }
    }

    override fun digestInto(dst: Writeable) {
        when (dst) {
            is MemoryWriteable -> dst.writeMemory {
                digestInto(it)
                digestSize
            }
            else -> dst.writeBytes(getDigest(), 0, digestSize)
        }
    }

    override fun close() {}
}

internal class MessageDigestJvmDigestWriteable(algorithmName: String) : AbstractJvmDigestWriteable() {
    private val messageDigest = MessageDigest.getInstance(algorithmName)

    override val digestSize: Int get() = messageDigest.digestLength

    override fun updateByte(byte: Byte) { messageDigest.update(byte) }
    override fun updateBytes(bytes: ByteArray, offset: Int, length: Int) { messageDigest.update(bytes, offset, length) }

    override fun digestInto(dst: ByteArray, dstOffset: Int) { messageDigest.digest(dst, dstOffset, digestSize) }
    override fun getDigest(): ByteArray = messageDigest.digest()

    override fun reset() { messageDigest.reset() }
}

public actual fun DigestWriteable.Companion.newInstance(algorithm: DigestAlgorithm): DigestWriteable =
        MessageDigestJvmDigestWriteable(when (algorithm) {
            DigestAlgorithm.SHA1 -> "SHA-1"
            DigestAlgorithm.SHA224 -> "SHA-224"
            DigestAlgorithm.SHA256 -> "SHA-256"
            DigestAlgorithm.SHA384 -> "SHA-384"
            DigestAlgorithm.SHA512 -> "SHA-512"
        })



internal class HmacJvmDigestWriteable(specs: SecretKeySpec) : AbstractJvmDigestWriteable() {
    private val mac = Mac.getInstance(specs.algorithm).also { it.init(specs) }

    override val digestSize: Int get() = mac.macLength

    override fun updateByte(byte: Byte) { mac.update(byte) }
    override fun updateBytes(bytes: ByteArray, offset: Int, length: Int) { mac.update(bytes, offset, length) }

    override fun digestInto(dst: ByteArray, dstOffset: Int) { mac.doFinal(dst, dstOffset) }
    override fun getDigest(): ByteArray = mac.doFinal()

    override fun reset() { mac.reset() }
}

public actual fun DigestWriteable.Companion.newHmacInstance(algorithm: DigestAlgorithm, key: ReadMemory): DigestWriteable {
    val algorithmName = when (algorithm) {
        DigestAlgorithm.SHA1   -> "HmacSHA1"
        DigestAlgorithm.SHA224 -> "HmacSHA224"
        DigestAlgorithm.SHA256 -> "HmacSHA256"
        DigestAlgorithm.SHA384 -> "HmacSHA384"
        DigestAlgorithm.SHA512 -> "HmacSHA512"
    }
    val secretKeySpec = when (key) {
        is ByteArrayMemory -> SecretKeySpec(key.array, key.offset, key.size, algorithmName)
        else -> SecretKeySpec(key.getBytes(), algorithmName)
    }
    return HmacJvmDigestWriteable(secretKeySpec)
}
