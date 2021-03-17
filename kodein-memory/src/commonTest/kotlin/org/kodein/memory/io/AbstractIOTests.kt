package org.kodein.memory.io

import org.kodein.memory.util.DeferScope
import kotlin.test.AfterTest


abstract class AbstractIOTests : DeferScope() {

    @AfterTest
    fun afterTest() {
        executeAllDeferred()
    }

}
