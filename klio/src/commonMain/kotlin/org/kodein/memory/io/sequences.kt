package org.kodein.memory.io


public fun Readable.asByteSequence(): Sequence<Byte> = sequence { while (valid()) yield(readByte()) }
public fun Readable.asShortSequence(): Sequence<Short> = sequence { while (valid()) yield(readShort()) }
public fun Readable.asIntSequence(): Sequence<Int> = sequence { while (valid()) yield(readInt()) }
public fun Readable.asLongSequence(): Sequence<Long> = sequence { while (valid()) yield(readLong()) }
public fun Readable.asFloatSequence(): Sequence<Float> = sequence { while (valid()) yield(readFloat()) }
public fun Readable.asDoubleSequence(): Sequence<Double> = sequence { while (valid()) yield(readDouble()) }

@ExperimentalUnsignedTypes
public fun Readable.asUByteSequence(): Sequence<UByte> = sequence { while (valid()) yield(readUByte()) }
@ExperimentalUnsignedTypes
public fun Readable.asUShortSequence(): Sequence<UShort> = sequence { while (valid()) yield(readUShort()) }
@ExperimentalUnsignedTypes
public fun Readable.asUIntSequence(): Sequence<UInt> = sequence { while (valid()) yield(readUInt()) }
@ExperimentalUnsignedTypes
public fun Readable.asULongSequence(): Sequence<ULong> = sequence { while (valid()) yield(readULong()) }
