package org.kodein.memory.crypto

import org.kodein.memory.io.Memory
import org.kodein.memory.io.array
import org.kodein.memory.use
import org.kodein.memory.util.UUID
import kotlin.experimental.and
import kotlin.experimental.or


public object UUIDv5Namespaces {
    public val DNS: UUID  = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
    public val URL: UUID  = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8")
    public val OID: UUID  = UUID.fromString("6ba7b812-9dad-11d1-80b4-00c04fd430c8")
    public val X500: UUID = UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430c8")
}

public fun UUID.Companion.v5From(namespace: UUID, bytes: Memory): UUID {
    DigestWriteable.newInstance(DigestAlgorithm.SHA1).use { sha1 ->
        sha1.writeLong(namespace.mostSignificantBits)
        sha1.writeLong(namespace.leastSignificantBits)
        sha1.writeBytes(bytes)

        val output = Memory.array(sha1.digestSize)
        sha1.digestInto(output)

        output[6] = 0x50.toByte() or (output[6] and 0xF0.toByte())
        output[8] = 0x80.toByte() or (output[8] and 0x3F.toByte())

        return UUID(output.getLong(0), output.getLong(8))
    }
}
