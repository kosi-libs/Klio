package org.kodein.memory.io


public sealed class OutOfMemoryException(msg: String) : IOException(msg) {
    public class NotEnoughRemaining(public val needed: Int, public val remaining: Int)
        : OutOfMemoryException("Buffer out of memory. Needed at least $needed remaining bytes, but has only $remaining bytes.")

    public class BufferTooLimited(public val needed: Int, public val limit: Int)
        : OutOfMemoryException("Buffer out of memory. Needed at least $needed total bytes, but is limited at $limit bytes.")

    public class ArrayTooSmall(public val needed: Int, public val size: Int)
        : OutOfMemoryException("Array too small. Needed at least $needed bytes, but array size is $size bytes.")
}
