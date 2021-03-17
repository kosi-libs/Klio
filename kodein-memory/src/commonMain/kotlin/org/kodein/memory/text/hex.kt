package org.kodein.memory.text

@OptIn(ExperimentalUnsignedTypes::class)
public fun ByteArray.toHexString(): String = asUByteArray().toHexString()

@OptIn(ExperimentalUnsignedTypes::class)
public fun UByteArray.toHexString(): String =
    joinToString("") { it.toString(16).padStart(2, '0') }
