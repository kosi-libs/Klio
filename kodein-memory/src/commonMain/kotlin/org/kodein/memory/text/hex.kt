package org.kodein.memory.text

@UseExperimental(ExperimentalUnsignedTypes::class)
fun ByteArray.toHexString() = joinToString("") {
    val str = it.toUByte().toString(16)
    if (str.length == 1) "0$str"
    else str
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun UByteArray.toHexString() = joinToString("") {
    val str = it.toString(16)
    if (str.length == 1) "0$str"
    else str
}
