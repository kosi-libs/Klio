package org.kodein.memory.file

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        PathsTests::class,
        FilesTests::class
)
class AndroidConnectedSuite {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUp() {
            FileSystem.registerContext(InstrumentationRegistry.getInstrumentation().targetContext)
        }
    }

}