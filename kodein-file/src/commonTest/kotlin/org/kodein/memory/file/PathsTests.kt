package org.kodein.memory.file

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PathsTests {

    @Test
    fun isAbsolute() {
        assertEquals(true, FileSystem.currentDirectory.isAbsolute())
        assertFalse(Path("something", "else").isAbsolute())
    }

    @Test
    fun rationalize() {
        assertEquals(Path("one", "two", "three"), Path("one", "other", "..", "two", ".", "three").rationalize())
        assertEquals(Path("..", ".."), Path("..", "..", "one", "..").rationalize())

        val root = FileSystem.roots[0]
        assertEquals(root, root.resolve("..", ".", "..").rationalize())
    }

    @Test
    fun toAbsolute() {
        val path = Path("something", "else")
        assertEquals(FileSystem.currentDirectory.path + Path.separator + "something" + Path.separator + "else", path.toAbsolute().path)
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

}
