package org.kodein.memory.io


fun Readable.asSequence(): Sequence<Byte> = sequence { while (valid()) yield(readByte()) }
fun Readable.asCharSequence(): Sequence<Char> = sequence { while (valid()) yield(readChar()) }
fun Readable.asShortSequence(): Sequence<Short> = sequence { while (valid()) yield(readShort()) }
fun Readable.asIntSequence(): Sequence<Int> = sequence { while (valid()) yield(readInt()) }
fun Readable.asLongSequence(): Sequence<Long> = sequence { while (valid()) yield(readLong()) }
fun Readable.asFloatSequence(): Sequence<Float> = sequence { while (valid()) yield(readFloat()) }
fun Readable.asDoubleSequence(): Sequence<Double> = sequence { while (valid()) yield(readDouble()) }

@ExperimentalUnsignedTypes
fun Readable.asUSequence(): Sequence<UByte> = sequence { while (valid()) yield(readUByte()) }
@ExperimentalUnsignedTypes
fun Readable.asUShortSequence(): Sequence<UShort> = sequence { while (valid()) yield(readUShort()) }
@ExperimentalUnsignedTypes
fun Readable.asUIntSequence(): Sequence<UInt> = sequence { while (valid()) yield(readUInt()) }
@ExperimentalUnsignedTypes
fun Readable.asULongSequence(): Sequence<ULong> = sequence { while (valid()) yield(readULong()) }

