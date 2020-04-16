package org.kodein.memory.file

import org.kodein.memory.io.readLine
import org.kodein.memory.text.putString
import org.kodein.memory.use
import org.kodein.memory.util.nextString
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FilesTests {

    @Test
    fun list() {
        val list = FileSystem.currentDirectory.listDir()
        assertTrue(FileSystem.currentDirectory.resolve("src") in list, "No \"src\" in $list")
        assertTrue(FileSystem.currentDirectory.resolve("build.gradle.kts") in list, "No \"build.gradle.kts\" in $list")
    }

    @Test
    fun resolveList() {
        val list = FileSystem.currentDirectory
                .resolve("src")
                .listDir()
        assertEquals(5, list.size, "$list")
        assertTrue(FileSystem.currentDirectory.resolve("src", "commonMain") in list)
        assertTrue(FileSystem.currentDirectory.resolve("src", "commonTest") in list)
        assertTrue(FileSystem.currentDirectory.resolve("src", "jvmMain") in list)
        assertTrue(FileSystem.currentDirectory.resolve("src", "macAndLinuxMain") in list)
        assertTrue(FileSystem.currentDirectory.resolve("src", "mingwX64Main") in list)
    }

    @Test
    fun listUtf() {
        val list = FileSystem.currentDirectory
                .resolve("src", "commonTest", "resources")
                .listDir()
        assertEquals(1, list.size, "$list")
        assertEquals("L'étranger de Camus.txt", list[0].name)
    }

    @Test
    fun getType() {
        val resources = FileSystem.currentDirectory.resolve("src", "commonTest", "resources")
        assertEquals(EntityType.Directory, resources.getType())
        assertEquals(EntityType.File.Regular, resources.resolve("L'étranger de Camus.txt").getType())
        assertEquals(EntityType.Non.Existent, resources.resolve("other").getType())
    }

    @Test
    fun read() {
        val txtFile = FileSystem.currentDirectory.resolve("src", "commonTest", "resources", "L'étranger de Camus.txt")
        txtFile.openReadableFile().use { r ->
            val firstLine = r.readLine()
            val secondLine = r.readLine()
            assertEquals(
                    "Aujourd'hui, maman est morte. Ou peut-être hier, je ne sais pas. J'ai reçu un télégramme de l'asile : « Mère décédée. Enterrement demain. Sentiments distingués. » Cela ne veut rien dire. C'était peut-être hier.",
                    firstLine
            )
            assertEquals(
                    "L'asile de vieillards est à Marengo, à quatre-vingts kilomètres d'Alger. Je prendrai l'autobus à deux heures et j'arriverai dans l'après-midi. Ainsi, je pourrai veiller et je rentrerai demain soir. J'ai demandé deux jours de congé à mon patron et il ne pouvait pas me les refuser avec une excuse pareille. Mais il n'avait pas l'air content. Je lui ai même dit : « Ce n'est pas de ma faute. » Il n'a pas répondu. J'ai pensé alors que je n'aurais pas dû lui dire cela. En somme, je n'avais pas à m'excuser. C'était plutôt à lui de me présenter ses condoléances. Mais il le fera sans doute après-demain, quand il me verra en deuil. Pour le moment, c'est un peu comme si maman n'était pas morte. Après l'enterrement, au contraire, ce sera une affaire classée et tout aura revêtu une allure plus officielle.",
                    secondLine
            )
        }
    }

    @Test
    fun writeTmp() {
        val tmpFile = FileSystem.tempDirectory.resolve("kodein-file-${Random.nextString(10)}")
        assertEquals(EntityType.Non.Existent, tmpFile.getType())
        tmpFile.openWriteableFile().use { w ->
            w.putString("First line\n")
            w.putString("Second line\n")
        }
        tmpFile.openReadableFile().use { r ->
            assertEquals("First line", r.readLine())
            assertEquals("Second line", r.readLine())
            assertNull(r.readLine())
        }
        tmpFile.openWriteableFile(append = true).use { w ->
            w.putString("Aujourd'hui, maman est morte. Ou peut-être hier, je ne sais pas.J'ai reçu un télégramme de l'asile : « Mère décédée. Enterrement demain. Sentiments distingués. » Cela ne veut rien dire. C'était peut-être hier.\n")
            w.putString("L'asile de vieillards est à Marengo, à quatre-vingts kilomètres d'Alger. Je prendrai l'autobus à deux heures et j'arriverai dans l'après-midi. Ainsi, je pourrai veiller et je rentrerai demain soir. J'ai demandé deux jours de congé à mon patron et il ne pouvait pas me les refuser avec une excuse pareille. Mais il n'avait pas l'air content. Je lui ai même dit : « Ce n'est pas de ma faute. » Il n'a pas répondu. J'ai pensé alors que je n'aurais pas dû lui dire cela. En somme, je n'avais pas à m'excuser. C'était plutôt à lui de me présenter ses condoléances. Mais il le fera sans doute après-demain, quand il me verra en deuil. Pour le moment, c'est un peu comme si maman n'était pas morte. Après l'enterrement, au contraire, ce sera une affaire classée et tout aura revêtu une allure plus officielle.\n")
        }
        tmpFile.openReadableFile().use { r ->
            assertEquals("First line", r.readLine())
            assertEquals("Second line", r.readLine())
            assertEquals("Aujourd'hui, maman est morte. Ou peut-être hier, je ne sais pas.J'ai reçu un télégramme de l'asile : « Mère décédée. Enterrement demain. Sentiments distingués. » Cela ne veut rien dire. C'était peut-être hier.", r.readLine())
            assertEquals("L'asile de vieillards est à Marengo, à quatre-vingts kilomètres d'Alger. Je prendrai l'autobus à deux heures et j'arriverai dans l'après-midi. Ainsi, je pourrai veiller et je rentrerai demain soir. J'ai demandé deux jours de congé à mon patron et il ne pouvait pas me les refuser avec une excuse pareille. Mais il n'avait pas l'air content. Je lui ai même dit : « Ce n'est pas de ma faute. » Il n'a pas répondu. J'ai pensé alors que je n'aurais pas dû lui dire cela. En somme, je n'avais pas à m'excuser. C'était plutôt à lui de me présenter ses condoléances. Mais il le fera sans doute après-demain, quand il me verra en deuil. Pour le moment, c'est un peu comme si maman n'était pas morte. Après l'enterrement, au contraire, ce sera une affaire classée et tout aura revêtu une allure plus officielle.", r.readLine())
            assertNull(r.readLine())
        }
        tmpFile.openWriteableFile(append = false).use { w ->
            w.putString("Third line\n")
            w.putString("Fourth line\n")
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

}