package org.kodein.memory.crypto

import org.kodein.memory.io.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.DestroyFailedException


internal class JvmCipherWriteable(private val cipher: javax.crypto.Cipher, private val secretKeySpec: SecretKeySpec, private val output: Writeable) : CipherWriteable {

    private var bytesWritten = 0

    override val position: Int get() = bytesWritten

    private val buffer = Memory.array(8)

    override fun requestCanWrite(needed: Int) {}

    override fun writeByte(value: Byte) {
        buffer.putByte(0, value)
        writeBytes(buffer.array, 0, 1)
    }

    override fun writeShort(value: Short) {
        buffer.putShort(0, value)
        writeBytes(buffer.array, 0, 2)
    }

    override fun writeInt(value: Int) {
        buffer.putInt(0, value)
        writeBytes(buffer.array, 0, 4)
    }

    override fun writeLong(value: Long) {
        buffer.putLong(0, value)
        writeBytes(buffer.array, 0, 8)
    }

    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
        if (output is MemoryWriteable) {
            val outputMemory = output.memory.internalMemory()
            if (outputMemory is ByteArrayMemory) {
                val count = cipher.update(src, srcOffset, length, outputMemory.array, outputMemory.offset + output.position)
                output.skip(count)
                bytesWritten += length
                return
            }
        }
        output.writeBytes(cipher.update(src, srcOffset, length))
        bytesWritten += length
    }

    override fun writeBytes(src: ReadMemory) {
        when (val srcMemory = src.internalMemory()) {
            is ByteArrayMemory -> writeBytes(srcMemory.array, srcMemory.offset, src.size)
            else -> writeBytes(srcMemory.getBytes())
        }
    }

    override fun writeBytes(src: Readable, length: Int) {
        if (src is MemoryReadable) writeBytes(src.readSlice(length))
        else writeBytes(src.readBytes(length))
    }

    override fun flush() {}

    override fun close() {
        if (output is MemoryWriteable) {
            val outputMemory = output.memory
            if (outputMemory is ByteArrayMemory) {
                val count = cipher.doFinal(outputMemory.array, outputMemory.offset + output.position)
                output.skip(count)
                return
            }
        }
        output.writeBytes(cipher.doFinal())
        secretKeySpec.safeDestroy()
    }
}

public actual object AES128 {
    private fun getCipher(cipherMode: CipherMode, opMode: Int, key: ReadMemory, output: Writeable): JvmCipherWriteable {
        require(key.size in arrayOf(16, 24, 32)) { "Key must be 16, 24 or 32 bytes (not ${key.size})." }

        val secretKeySpec = when (val skMemory = key.internalMemory()) {
            is ByteArrayMemory -> SecretKeySpec(skMemory.array, skMemory.offset, skMemory.size, "AES")
            else -> SecretKeySpec(skMemory.getBytes(), "AES")
        }

        val modeName = when (cipherMode) {
            CipherMode.ECB -> "ECB"
            is CipherMode.CBC -> "CBC"
        }

        val cipher = Cipher.getInstance("AES/$modeName/PKCS5Padding")

        val ivSpec: IvParameterSpec? = when (cipherMode) {
            is CipherMode.CBC -> {
                cipherMode.iv?.let { iv ->
                    require(iv.size == 16) { "IV must be 16 bytes." }
                    when (iv) {
                        is ByteArrayMemory -> IvParameterSpec(iv.array, iv.offset, 16)
                        else -> IvParameterSpec(iv.getBytes())
                    }
                }
            }
            CipherMode.ECB -> null
        }

        cipher.init(opMode, secretKeySpec, ivSpec)

        return JvmCipherWriteable(cipher, secretKeySpec, output)
    }

    public actual fun encrypt(mode: CipherMode, key: ReadMemory, output: Writeable): CipherWriteable =
        getCipher(mode, Cipher.ENCRYPT_MODE, key, output)

    public actual fun decrypt(mode: CipherMode, key: ReadMemory, output: Writeable): CipherWriteable =
        getCipher(mode, Cipher.DECRYPT_MODE, key, output)
}
