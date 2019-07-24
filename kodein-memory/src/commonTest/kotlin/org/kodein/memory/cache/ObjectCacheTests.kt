package org.kodein.memory.cache

import kotlin.test.*

class ObjectCacheTests  {

    private inline fun <reified T : Any> assertIs(value: Any) = asserter.assertTrue({ "Expected $value to be of type ${T::class} but is ${value::class}" }, value is T)

    @Test
    fun putGetDeleteRemove() {
        val cache = ObjectCache<String, String>(1024)

        assertEquals(0, cache.entryCount)
        assertEquals(0, cache.missCount)
        assertIs<ObjectCache.Entry.NotInCache>(cache.getEntry("name"))
        assertEquals(1, cache.missCount)
        assertNull(cache["name"])
        assertEquals(2, cache.missCount)

        assertEquals(0, cache.entryCount)
        assertEquals(0, cache.putCount)
        cache.put("name", "Salomon", 7)
        assertEquals(1, cache.entryCount)
        assertEquals(1, cache.putCount)
        assertEquals(0, cache.hitCount)
        assertIs<ObjectCache.Entry.Cached<*>>(cache.getEntry("name"))
        assertEquals(1, cache.hitCount)
        assertEquals("Salomon", cache["name"])
        assertEquals(2, cache.hitCount)
        assertEquals(15, cache.size)

        assertEquals(0, cache.deleteCount)
        cache.delete("name")
        assertEquals(1, cache.entryCount)
        assertEquals(1, cache.deleteCount)
        assertIs<ObjectCache.Entry.Deleted>(cache.getEntry("name"))
        assertEquals(3, cache.hitCount)
        assertNull(cache["name"])
        assertEquals(4, cache.hitCount)
        assertEquals(8, cache.size)

        assertEquals(0, cache.evictionCount)
        cache.evict("name")
        assertEquals(0, cache.entryCount)
        assertEquals(1, cache.evictionCount)
        assertIs<ObjectCache.Entry.NotInCache>(cache.getEntry("name"))
        assertEquals(3, cache.missCount)
        assertNull(cache["name"])
        assertEquals(4, cache.missCount)
        assertEquals(0, cache.size)
    }

    @Test
    fun getOrRetrieve() {
        val cache = ObjectCache<String, String>(1024)

        assertEquals(0, cache.retrieveCount)
        cache.getOrRetrieve("name") { Sized("Salomon", 7) }
        assertEquals(1, cache.retrieveCount)
        assertEquals("Salomon", cache["name"])
    }

    @Test
    fun evict() {
        val cache = ObjectCache<String, String>(100)
        cache.put("1", "O", 50)
        assertEquals("O", cache["1"])
        cache.put("2", "T", 50)
        assertEquals("T", cache["2"])
        assertNull(cache["1"])
        assertIs<ObjectCache.Entry.NotInCache>(cache.getEntry("1"))
    }

    @Test
    fun copyPutInCopy() {
        val cache = ObjectCache<String, String>(1024)
        cache.put("me", "Salomon", 7)

        val copy = cache.newCopy(512)

        assertEquals("Salomon", copy["me"])
        assertEquals(1024, copy.maxSize)

        copy.put("her", "Laila", 5)
        assertEquals("Laila", copy["her"])
        assertNull(cache["her"])
        assertEquals(512, copy.maxSize)
    }

    @Test
    fun copyPutInOriginal() {
        val cache = ObjectCache<String, String>(1024)
        cache.put("me", "Salomon", 7)

        val copy = cache.newCopy(512)

        assertEquals("Salomon", copy["me"])
        assertEquals(1024, copy.maxSize)

        cache.put("her", "Laila", 5)
        assertEquals("Laila", cache["her"])
        assertNull(copy["her"])
        assertEquals(512, copy.maxSize)
    }

    @Test
    fun clean() {
        val cache = ObjectCache<String, String>(1024)
        cache.put("me", "Salomon", 7)
        cache.put("her", "laila", 5)

        assertEquals(2, cache.entryCount)
        assertEquals(28, cache.size)

        cache.clean()

        assertEquals(0, cache.entryCount)
        assertEquals(0, cache.size)
    }
}
