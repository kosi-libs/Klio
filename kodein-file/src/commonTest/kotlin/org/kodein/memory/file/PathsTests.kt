package org.kodein.memory.file

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PathsTests {

    @Test
    fun isAbsolute() {
        assertEquals(true, FileSystem.currentDirectory.isAbsolute(), "${FileSystem.currentDirectory} is not absolute")
        assertFalse(Path("something", "else").isAbsolute())
    }

    @Test
    fun normalize() {
        assertEquals(Path("one", "two", "three"), Path("one", "other", "..", "two", ".", "three").normalize())
        assertEquals(Path("..", ".."), Path("..", "..", "one", "..").normalize())

        val root = FileSystem.roots[0]
        assertEquals(root, root.resolve("..", ".", "..").normalize())

        assertEquals(Path("a", "b"), Path("a${Path.separator}${Path.separator}b").normalize())
    }

    @Test
    fun toAbsolute() {
        assertEquals(FileSystem.currentDirectory.resolve("a", "b"), Path("a", "b").toAbsolute())
        assertEquals(FileSystem.roots[0].resolve("a", "b"), FileSystem.roots[0].resolve("a", "b").toAbsolute())
    }

    @Test
    fun parent() {
        val rel = Path("a", "b", "c")
        assertEquals(Path("a", "b"), rel.parent())
        assertEquals(Path("a"), rel.parent().parent())
        assertEquals(Path("."), rel.parent().parent().parent())
        assertEquals(Path(".."), rel.parent().parent().parent().parent())
        assertEquals(Path("..", ".."), rel.parent().parent().parent().parent().parent())

        val abs = FileSystem.roots[0].resolve("a", "b", "c")
        assertEquals(FileSystem.roots[0].resolve("a", "b"), abs.parent())
        assertEquals(FileSystem.roots[0].resolve("a"), abs.parent().parent())
        assertEquals(FileSystem.roots[0], abs.parent().parent().parent())
        assertEquals(FileSystem.roots[0], abs.parent().parent().parent().parent())
    }

    @Test
    fun name() {
        assertEquals("c", Path("a", "b", "c").name)
    }
}
