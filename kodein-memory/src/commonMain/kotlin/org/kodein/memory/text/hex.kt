package org.kodein.memory.text

@OptIn(ExperimentalUnsignedTypes::class)
public fun ByteArray.toHexString(): String = joinToString("") {
    val str = it.toUByte().toString(16)
    if (str.length == 1) "0$str"
    else str
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun UByteArray.toHexString(): String = joinToString("") {
    val str = it.toString(16)
    if (str.length == 1) "0$str"
    else str
}
