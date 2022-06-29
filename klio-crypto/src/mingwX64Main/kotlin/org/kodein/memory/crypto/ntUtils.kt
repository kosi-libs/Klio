package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.io.IOException
import platform.windows.*

@OptIn(ExperimentalUnsignedTypes::class)
internal fun NTSTATUS.requireNTSuccess(name: String) {
    if (this >= 0) return

    memScoped {
        val hDLL = LoadLibrary!!("NTDLL.DLL".wcstr.getPointer(this))
        val messagePtr = nativeHeap.alloc<LPVOIDVar>()

        FormatMessageA(
            (FORMAT_MESSAGE_ALLOCATE_BUFFER or FORMAT_MESSAGE_FROM_SYSTEM or FORMAT_MESSAGE_FROM_HMODULE).toUInt(),
            hDLL,
            this@requireNTSuccess.toUInt(),
            ((SUBLANG_DEFAULT shl 10) or LANG_NEUTRAL).toUInt(),
            messagePtr.ptr.reinterpret(),
            0u,
            null
        )

        val message = messagePtr.value!!.reinterpret<ByteVar>().toKString()
        LocalFree(messagePtr.value)
        FreeLibrary(hDLL)

        throw IOException("$name: $message")
    }
}
