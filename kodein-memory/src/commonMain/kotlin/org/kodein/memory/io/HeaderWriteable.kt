//package org.kodein.memory.io
//
//
//public class HeaderWriteable(private val headerSize: Int, private val newMemory: (Int) -> Memory, private val getWriteable: (ReadMemory) -> Writeable): Writeable {
//
//    private sealed class State {
//        class Header(val header: MemoryWriteable) : State()
//        class Body(val body: Writeable) : State()
//    }
//
//    private var state: State = State.Header(MemoryWriteable(newMemory(headerSize)), getWriteable)
//
//    private val buffer = newMemory(8)
//
//    private fun checkState() {
//        val s = state
//        if (s is State.Header && s.header.remaining == 0) {
//            state = State.Body(getWriteable(s.header.memory))
//        }
//    }
//
//    override val position: Int get() = when (val s = state) {
//        is State.Header -> s.header.position
//        is State.Body -> headerSize + s.body.position
//    }
//
//    override fun requestCanWrite(needed: Int) {
//        when (val s = state) {
//            is State.Header -> {}
//            is State.Body -> s.body.requestCanWrite(needed)
//        }
//    }
//
//    override fun writeByte(value: Byte) {
//        when (val s = state) {
//            is State.Header -> {
//                s.header.writeByte(value)
//                checkState()
//            }
//            is State.Body -> s.body.writeByte(value)
//        }
//    }
//
//    private inline fun <T> writeValue(value: T, size: Int, write: Writeable.(T) -> Unit, put: Memory.(Int, T) -> Unit) {
//        when (val s = state) {
//            is State.Header -> {
//                if (s.header.remaining <= size) {
//                    s.header.write(value)
//                    checkState()
//                } else {
//                    buffer.put(0, value)
//                    writeBytes(buffer.slice(0, size))
//                }
//            }
//            is State.Body -> s.body.write(value)
//        }
//    }
//    override fun writeShort(value: Short): Unit = writeValue(value, 2, Writeable::writeShort, Memory::putShort)
//    override fun writeInt(value: Int): Unit = writeValue(value, 4, Writeable::writeInt, Memory::putInt)
//    override fun writeLong(value: Long): Unit = writeValue(value, 8, Writeable::writeLong, Memory::putLong)
//
//    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
//        when (val s = state) {
//            is State.Header -> {
//                val remaining = s.header.remaining
//                if (remaining <= length) {
//                    s.header.writeBytes(src, srcOffset, length)
//                    checkState()
//                } else {
//                    s.header.writeBytes(src, srcOffset, remaining)
//                    checkState()
//                    writeBytes(src, srcOffset + remaining, length - remaining)
//                }
//            }
//            is State.Body -> s.body.writeBytes(src, srcOffset, length)
//        }
//    }
//
//    override fun writeBytes(src: ReadMemory) {
//        when (val s = state) {
//            is State.Header -> {
//                val remaining = s.header.remaining
//                if (remaining <= src.size) {
//                    s.header.writeBytes(src)
//                    checkState()
//                } else {
//                    s.header.writeBytes(src.slice(0, remaining))
//                    checkState()
//                    writeBytes(src.sliceAt(remaining))
//                }
//            }
//            is State.Body -> s.body.writeBytes(src)
//        }
//    }
//
//    override fun writeBytes(src: Readable, length: Int) {
//        when (val s = state) {
//            is State.Header -> {
//                val remaining = s.header.remaining
//                if (remaining <= length) {
//                    s.header.writeBytes(src, length)
//                    checkState()
//                } else {
//                    s.header.writeBytes(src, remaining)
//                    checkState()
//                    writeBytes(src, length - remaining)
//                }
//            }
//            is State.Body -> s.body.writeBytes(src, length)
//        }
//    }
//
//    override fun flush() {
//        when ()
//    }
//
//
//}
