package org.kodein.memory.crypto

import org.kodein.memory.io.IOException
import platform.CoreCrypto.*


internal fun Int.requireCoreCryptoSuccess(name: String) {
    when (this) {
        kCCSuccess -> return
        kCCParamError -> throw IOException("Core Crypto $name error: Illegal parameter value.")
        kCCBufferTooSmall -> throw IOException("Core Crypto $name error: Insufficent buffer provided for specified operation.")
        kCCMemoryFailure -> throw IOException("Core Crypto $name error: Memory allocation failure.")
        kCCAlignmentError -> throw IOException("Core Crypto $name error: Input size was not aligned properly.")
        kCCDecodeError -> throw IOException("Core Crypto $name error: Input data did not decode or decrypt properly.")
        kCCUnimplemented -> throw IOException("Core Crypto $name error: Function not implemented for the current algorithm.")
        else -> throw IOException("Core Crypto $name error: Unknown")
    }
}
