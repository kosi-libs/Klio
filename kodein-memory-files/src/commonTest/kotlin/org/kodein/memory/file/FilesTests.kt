package org.kodein.memory.file

import org.kodein.memory.text.readLine
import org.kodein.memory.text.writeString
import org.kodein.memory.use
import org.kodein.memory.util.nextString
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FilesTests {

    @Test
    fun writeTmp() {
        val tmpFile = FileSystem.tempDirectory.resolve("kodein-file-${Random.nextString(10)}")
        assertEquals(EntityType.Non.Existent, tmpFile.getType())
        tmpFile.openWriteableFile().use { w ->
            w.writeString("First line\n")
            w.writeString("Second line\n")
        }
        tmpFile.openReadableFile().use { r ->
            assertEquals("First line", r.readLine())
            assertEquals("Second line", r.readLine())
            assertNull(r.readLine())
        }
        tmpFile.openWriteableFile(append = true).use { w ->
            w.writeString("Aujourd'hui, maman est morte. Ou peut-être hier, je ne sais pas.J'ai reçu un télégramme de l'asile : « Mère décédée. Enterrement demain. Sentiments distingués. » Cela ne veut rien dire. C'était peut-être hier.\n")
            w.writeString("L'asile de vieillards est à Marengo, à quatre-vingts kilomètres d'Alger. Je prendrai l'autobus à deux heures et j'arriverai dans l'après-midi. Ainsi, je pourrai veiller et je rentrerai demain soir. J'ai demandé deux jours de congé à mon patron et il ne pouvait pas me les refuser avec une excuse pareille. Mais il n'avait pas l'air content. Je lui ai même dit : « Ce n'est pas de ma faute. » Il n'a pas répondu. J'ai pensé alors que je n'aurais pas dû lui dire cela. En somme, je n'avais pas à m'excuser. C'était plutôt à lui de me présenter ses condoléances. Mais il le fera sans doute après-demain, quand il me verra en deuil. Pour le moment, c'est un peu comme si maman n'était pas morte. Après l'enterrement, au contraire, ce sera une affaire classée et tout aura revêtu une allure plus officielle.\n")
        }
        tmpFile.openReadableFile().use { r ->
            assertEquals("First line", r.readLine())
            assertEquals("Second line", r.readLine())
            assertEquals("Aujourd'hui, maman est morte. Ou peut-être hier, je ne sais pas.J'ai reçu un télégramme de l'asile : « Mère décédée. Enterrement demain. Sentiments distingués. » Cela ne veut rien dire. C'était peut-être hier.", r.readLine())
            assertEquals("L'asile de vieillards est à Marengo, à quatre-vingts kilomètres d'Alger. Je prendrai l'autobus à deux heures et j'arriverai dans l'après-midi. Ainsi, je pourrai veiller et je rentrerai demain soir. J'ai demandé deux jours de congé à mon patron et il ne pouvait pas me les refuser avec une excuse pareille. Mais il n'avait pas l'air content. Je lui ai même dit : « Ce n'est pas de ma faute. » Il n'a pas répondu. J'ai pensé alors que je n'aurais pas dû lui dire cela. En somme, je n'avais pas à m'excuser. C'était plutôt à lui de me présenter ses condoléances. Mais il le fera sans doute après-demain, quand il me verra en deuil. Pour le moment, c'est un peu comme si maman n'était pas morte. Après l'enterrement, au contraire, ce sera une affaire classée et tout aura revêtu une allure plus officielle.", r.readLine())
            assertNull(r.readLine())
        }
        tmpFile.openWriteableFile(append = false).use { w ->
            w.writeString("Third line\n")
            w.writeString("Fourth line\n")
        }
        tmpFile.openReadableFile().use { r ->
            assertEquals("Third line", r.readLine())
            assertEquals("Fourth line", r.readLine())
            assertNull(r.readLine())
        }
        assertEquals(EntityType.File.Regular, tmpFile.getType())
        tmpFile.delete()
        assertEquals(EntityType.Non.Existent, tmpFile.getType())
    }

    @Test
    fun createDirs() {
        val tmpDirRoot = FileSystem.tempDirectory.resolve("kodein-dir-${Random.nextString(10)}")
        assertEquals(EntityType.Non.Existent, tmpDirRoot.getType())
        tmpDirRoot.createDir()
        assertEquals(EntityType.Directory, tmpDirRoot.getType())
        tmpDirRoot.delete()
        assertEquals(EntityType.Non.Existent, tmpDirRoot.getType())

        val tmpDirInter = tmpDirRoot.resolve("a")
        val tmpDirFinal = tmpDirInter.resolve("b")
        assertEquals(EntityType.Non.Existent, tmpDirRoot.getType())
        assertEquals(EntityType.Non.Existent, tmpDirInter.getType())
        assertEquals(EntityType.Non.Existent, tmpDirFinal.getType())
        tmpDirFinal.createDirs()
        assertEquals(EntityType.Directory, tmpDirRoot.getType())
        assertEquals(EntityType.Directory, tmpDirInter.getType())
        assertEquals(EntityType.Directory, tmpDirFinal.getType())
        tmpDirRoot.deleteRecursive()
        assertEquals(EntityType.Non.Existent, tmpDirRoot.getType())
        assertEquals(EntityType.Non.Existent, tmpDirInter.getType())
        assertEquals(EntityType.Non.Existent, tmpDirFinal.getType())
    }

    @Test
    fun list() {
        val tmpDir = FileSystem.tempDirectory.resolve("kodein-list-${Random.nextString(10)}")

        tmpDir.createDir()
        tmpDir.resolve("L'étranger.txt").openWriteableFile().use { it.writeString("Aujourd'hui, maman est morte.") }
        tmpDir.resolve("Lâïlã & §àlômoñ").createDir()

        val list = tmpDir.listDir()
        assertEquals(2, list.size)
        assertTrue(tmpDir.resolve("L'étranger.txt") in list)
        assertEquals(EntityType.File.Regular, tmpDir.resolve("L'étranger.txt").getType())
        assertTrue(tmpDir.resolve("Lâïlã & §àlômoñ") in list)
        assertEquals(EntityType.Directory, tmpDir.resolve("Lâïlã & §àlômoñ").getType())
        assertEquals(EntityType.Non.Existent, tmpDir.resolve("Laïla & Salomon").getType())

        tmpDir.deleteRecursive()
    }

}