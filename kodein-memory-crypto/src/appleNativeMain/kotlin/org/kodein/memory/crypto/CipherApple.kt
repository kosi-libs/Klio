package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.io.*
import org.kodein.memory.useOrNull
import platform.CoreCrypto.*
import platform.posix.size_tVar


private fun CCCryptorStatus.check() {
    when (this) {
        kCCSuccess -> return
        kCCParamError -> throw IOException("Illegal parameter value.")
        kCCBufferTooSmall -> throw IOException("Insufficent buffer provided for specified operation.")
        kCCMemoryFailure -> throw IOException("Memory allocation failure.")
        kCCAlignmentError -> throw IOException("Input size was not aligned properly.")
        kCCDecodeError -> throw IOException("Input data did not decode or decrypt properly.")
        kCCUnimplemented -> throw IOException("Function not implemented for the current algorithm.")
        else -> throw IOException("Unknown error code: $this")
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
internal class NativeCipherWriteable(private val cryptorRef: CCCryptorRefVar, private val key: Allocation, private val output: Writeable) : CipherWriteable {

    private var bytesWritten = 0

    override val position: Int get() = bytesWritten

    private val moved = nativeHeap.alloc<size_tVar>()

    private val buffer = Allocation.native(8)

    override fun requestCanWrite(needed: Int) {}

    override fun writeByte(value: Byte) {
        buffer.putByte(0, value)
        update(buffer.memory.pointer, 1)
    }

    override fun writeShort(value: Short) {
        buffer.putShort(0, value)
        update(buffer.memory.pointer, 2)
    }

    override fun writeInt(value: Int) {
        buffer.putInt(0, value)
        update(buffer.memory.pointer, 4)
    }

    override fun writeLong(value: Long) {
        buffer.putLong(0, value)
        update(buffer.memory.pointer, 8)
    }

    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
        require(srcOffset + length <= src.size)
        src.usePinned { pinned ->
            update(pinned.addressOf(srcOffset), length)
        }
    }

    override fun writeBytes(src: ReadMemory) {
        when (val srcMemory = src.internalMemory()) {
            is ByteArrayMemory -> writeBytes(srcMemory.array, srcMemory.offset, srcMemory.size)
            is CPointerMemory -> update(srcMemory.pointer, srcMemory.size)
            else -> writeBytes(srcMemory.getBytes())
        }
    }

    override fun writeBytes(src: Readable, length: Int) {
        if (src is MemoryReadable) writeBytes(src.readMemory(length))
        else writeBytes(src.readBytes(length))
    }

    private inline fun update(maxOutputLength: Int, crossinline doUpdate: (CPointer<*>, Int) -> Unit) {
        val written = if (output is MemoryWriteable) {
            output.writeMemory {
                when (val outputMemory = it.internalMemory()) {
                    is ByteArrayMemory -> outputMemory.array.usePinned { pinned ->
                        doUpdate(pinned.addressOf(outputMemory.offset), outputMemory.size)
                        moved.value.toInt()
                    }
                    is CPointerMemory -> {
                        doUpdate(outputMemory.pointer, outputMemory.size)
                        moved.value.toInt()
                    }
                    else -> -1
                }
            }
        } else -1
        if (written == -1) {
            val buffer = ByteArray(maxOutputLength)
            val count = buffer.usePinned { pinned ->
                doUpdate(pinned.addressOf(0), buffer.size)
                moved.value.toInt()
            }
            output.writeBytes(buffer, 0, count)
        }
    }

    private fun update(src: CPointer<*>, length: Int) =
        update(((length / 16) + 2) * 16) { output, outAvailable ->
            CCCryptorUpdate(cryptorRef.value, src, length.convert(), output, outAvailable.convert(), moved.ptr)
        }

    override fun flush() {}

    override fun close() {
        update(16) { output, outAvailable ->
            CCCryptorFinal(cryptorRef.value, output, outAvailable.convert(), moved.ptr)
        }

        key.fill(0)
        key.close()
        CCCryptorRelease(cryptorRef.value)
        nativeHeap.free(cryptorRef)
        buffer.close()
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
public actual object AES128 {
    private fun getCipher(cipherMode: CipherMode, opMode: UInt, key: ReadMemory, output: Writeable): NativeCipherWriteable {
        require(key.size in arrayOf(16, 24, 32)) { "Key must be 16, 24 or 32 bytes (not ${key.size})." }

        val keyCopy = Allocation.nativeCopy(key)

        val additionalOption = when (cipherMode) {
            is CipherMode.CBC -> 0u
            is CipherMode.ECB -> kCCOptionECBMode
        }

        val iv = when (cipherMode) {
            is CipherMode.CBC -> {
                cipherMode.iv?.let { iv ->
                    require(iv.size == 16) { "IV must be 16 bytes." }
                    Allocation.nativeCopy(iv)
                }
            }
            CipherMode.ECB -> null
        }

        iv.useOrNull {
            val cryptorRef = nativeHeap.alloc<CCCryptorRefVar>()

            CCCryptorCreate(
                opMode,
                kCCAlgorithmAES128,
                kCCOptionPKCS7Padding or additionalOption,
                keyCopy.memory.pointer,
                keyCopy.size.convert(),
                iv?.memory?.pointer,
                cryptorRef.ptr
            ).check()

            return NativeCipherWriteable(cryptorRef, keyCopy, output)
        }
    }

    public actual fun encrypt(mode: CipherMode, key: ReadMemory, output: Writeable): CipherWriteable =
        getCipher(mode, kCCEncrypt, key, output)

    public actual fun decrypt(mode: CipherMode, key: ReadMemory, output: Writeable): CipherWriteable =
        getCipher(mode, kCCDecrypt, key, output)
}
