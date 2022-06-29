package org.kodein.memory.crypto

import org.kodein.memory.Closeable
import org.kodein.memory.io.*
import org.kodein.memory.use


public sealed class CipherMode {
    public class CBC(public val iv: ReadMemory?) : CipherMode()
    public object ECB : CipherMode()
}

public fun aesEncryptedSize(clearLength: Int): Int = ((clearLength / 16) + 1) * 16


public interface CipherWriteable : Writeable, Closeable

public expect object AES128 {
    public fun encrypt(mode: CipherMode, key: ReadMemory, output: Writeable): CipherWriteable
    public fun decrypt(mode: CipherMode, key: ReadMemory, output: Writeable): CipherWriteable
}

public inline fun AES128.encrypt(mode: CipherMode, key: ReadMemory, output: Writeable, write: Writeable.() -> Unit): Unit =
    encrypt(mode, key, output).use(write)

public fun AES128.encrypt(mode: CipherMode, key: ReadMemory, input: ByteArray, inputOffset: Int, inputLength: Int): ByteArrayMemory {
    val output = Memory.array(aesEncryptedSize(inputLength))
    encrypt(mode, key, output.asWriteable()) { writeBytes(input, inputOffset, inputLength) }
    return output
}

public fun AES128.encrypt(mode: CipherMode, key: ReadMemory, input: ByteArray): ByteArrayMemory =
    encrypt(mode, key, input, 0, input.size)

public fun AES128.encrypt(mode: CipherMode, key: ReadMemory, input: Memory): ByteArrayMemory {
    val output = Memory.array(aesEncryptedSize(input.size))
    encrypt(mode, key, output.asWriteable()) { writeBytes(input) }
    return output
}

public fun AES128.encrypt(mode: CipherMode, key: ReadMemory, input: Readable, inputLength: Int): ByteArrayMemory {
    val output = Memory.array(aesEncryptedSize(inputLength))
    encrypt(mode, key, output.asWriteable()) { writeBytes(input, inputLength) }
    return output
}

public fun AES128.encrypt(mode: CipherMode, key: ReadMemory, input: CursorReadable): ByteArrayMemory =
    encrypt(mode, key, input, input.remaining)


public inline fun AES128.decrypt(mode: CipherMode, key: ReadMemory, output: Writeable, write: Writeable.() -> Unit): Unit =
    decrypt(mode, key, output).use(write)

public fun AES128.decrypt(mode: CipherMode, key: ReadMemory, input: ByteArray, inputOffset: Int, inputLength: Int): ByteArrayMemory {
    val output = Memory.array(aesEncryptedSize(inputLength))
    return output.slice {
        decrypt(mode, key, this) { writeBytes(input, inputOffset, inputLength) }
    }
}

public fun AES128.decrypt(mode: CipherMode, key: ReadMemory, input: ByteArray): ByteArrayMemory =
    decrypt(mode, key, input, 0, input.size)

public fun AES128.decrypt(mode: CipherMode, key: ReadMemory, input: Memory): ByteArrayMemory {
    val output = Memory.array(aesEncryptedSize(input.size))
    return output.slice {
        decrypt(mode, key, this) { writeBytes(input) }
    }
}

public fun AES128.decrypt(mode: CipherMode, key: ReadMemory, input: Readable, inputLength: Int): ByteArrayMemory {
    val output = Memory.array(aesEncryptedSize(inputLength))
    return output.slice {
        decrypt(mode, key, this) { writeBytes(input, inputLength) }
    }
}

public fun AES128.decrypt(mode: CipherMode, key: ReadMemory, input: CursorReadable): ByteArrayMemory =
    decrypt(mode, key, input, input.remaining)
