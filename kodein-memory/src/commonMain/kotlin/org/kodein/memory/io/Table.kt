package org.kodein.memory.io

import org.kodein.memory.text.getString
import org.kodein.memory.text.readString

@UseExperimental(ExperimentalUnsignedTypes::class)
class Table internal constructor(private val buffer: ReadBuffer) {

    companion object {
        const val TYPE_BYTE   = 'b'.toByte()
        const val TYPE_CHAR   = 'c'.toByte()
        const val TYPE_SHORT  = 's'.toByte()
        const val TYPE_INT    = 'i'.toByte()
        const val TYPE_LONG   = 'l'.toByte()
        const val TYPE_FLOAT  = 'f'.toByte()
        const val TYPE_DOUBLE = 'd'.toByte()

        const val TYPE_BOOLEANS = '?'.toByte()
        const val TYPE_STRING = 'S'.toByte()
        const val TYPE_BYTES  = 'B'.toByte()
//        const val TYPE_TABLE  = 'T'.toByte()
    }

    inner class Entry(
            val index: Int,
            val name: String,
            val type: Byte,
            val size: Int,
            val position: Int
    )

    private val byIndex = ArrayList<Entry>()
    private val byName = HashMap<String, Entry>()

    init {
        var index = 0
        while (buffer.hasRemaining()) {
            val nameSize = buffer.readUByte().toInt()
            val name = buffer.readString(sizeBytes = nameSize)
            val type = buffer.read()
            val size = when (type) {
                TYPE_BYTE -> Byte.SIZE_BYTES
                TYPE_CHAR -> Char.SIZE_BYTES
                TYPE_SHORT -> Short.SIZE_BYTES
                TYPE_INT -> Int.SIZE_BYTES
                TYPE_LONG -> Long.SIZE_BYTES
                TYPE_FLOAT -> Int.SIZE_BYTES
                TYPE_DOUBLE -> Long.SIZE_BYTES
                else -> {
                    buffer.readUShort().toInt()
                }
            }
            val entry = Entry(index++, name, type, size, buffer.position)
            byIndex.add(entry)
            byName.put(name, entry)
            buffer.skip(size)
        }
    }

    fun entry(index: Int) = byIndex[index]
    fun entry(name: String) = byName[name]

    val size get() = byIndex.size
    val lastIndex: Int get() = byIndex.lastIndex
    val names get() = byName.keys

    fun name(index: Int) = byIndex[index].name
    fun index(name: String) = byName[name]?.index ?: throw NoSuchElementException(name)

    fun byte(index: Int) = buffer[byIndex[index].position]
    fun byte(name: String) = byName[name]?.position?.let { buffer[it] } ?: throw NoSuchElementException(name)
    fun char(index: Int) = buffer.getChar(byIndex[index].position)
    fun char(name: String) = byName[name]?.position?.let { buffer.getChar(it) } ?: throw NoSuchElementException(name)
    fun short(index: Int) =  buffer.getShort(byIndex[index].position)
    fun short(name: String) = byName[name]?.position?.let { buffer.getShort(it) } ?: throw NoSuchElementException(name)
    fun int(index: Int) =  buffer.getInt(byIndex[index].position)
    fun int(name: String) = byName[name]?.position?.let { buffer.getInt(it) } ?: throw NoSuchElementException(name)
    fun long(index: Int) =  buffer.getLong(byIndex[index].position)
    fun long(name: String) = byName[name]?.position?.let { buffer.getLong(it) } ?: throw NoSuchElementException(name)
    fun float(index: Int) =  buffer.getFloat(byIndex[index].position)
    fun float(name: String) = byName[name]?.position?.let { buffer.getFloat(it) } ?: throw NoSuchElementException(name)
    fun double(index: Int) =  buffer.getDouble(byIndex[index].position)
    fun double(name: String) = byName[name]?.position?.let { buffer.getDouble(it) } ?: throw NoSuchElementException(name)

    fun string(index: Int) = byIndex[index].let { buffer.getString(it.position, sizeBytes = it.size) }
    fun string(name: String) = byName[name]?.let { buffer.getString(it.position, sizeBytes = it.size) } ?: throw NoSuchElementException(name)

    private fun booleans(entry: Entry): BooleanArray {
        val array = BooleanArray(entry.size * 8)
        for (num in 0 until entry.size) {
            val byte = buffer[entry.position + num].toInt()
            array[num * 8 + 0] = ((byte ushr 7) and 1) == 1
            array[num * 8 + 1] = ((byte ushr 6) and 1) == 1
            array[num * 8 + 2] = ((byte ushr 5) and 1) == 1
            array[num * 8 + 3] = ((byte ushr 4) and 1) == 1
            array[num * 8 + 4] = ((byte ushr 3) and 1) == 1
            array[num * 8 + 5] = ((byte ushr 2) and 1) == 1
            array[num * 8 + 6] = ((byte ushr 1) and 1) == 1
            array[num * 8 + 7] = ((byte ushr 0) and 1) == 1
        }
        return array
    }
    fun booleans(index: Int) = booleans(byIndex[index])
    fun booleans(name: String) = byName[name]?.let { booleans(it) } ?: throw NoSuchElementException(name)

    fun bytes(index: Int) = byIndex[index].let { buffer.getBytes(it.position, it.size) }
    fun bytes(name: String) = byName[name]?.let { buffer.getBytes(it.position, it.size) } ?: throw NoSuchElementException(name)

}

fun ReadBuffer.readTable(): Table = Table(this)
