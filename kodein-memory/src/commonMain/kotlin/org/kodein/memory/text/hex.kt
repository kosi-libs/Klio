package org.kodein.memory.text

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toHexString() = joinToString("") {
    val str = it.toUByte().toString(16)
    if (str.length == 1) "0$str"
    else str
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toHexString() = joinToString("") {
    val str = it.toString(16)
    if (str.length == 1) "0$str"
    else str
}
