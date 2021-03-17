package org.kodein.memory.util

import org.kodein.memory.Closeable
import org.kodein.memory.use


private typealias Deferred = () -> Unit

public open class DeferScope {

    @PublishedApi
    internal var topDeferred: Deferred? = null

    internal fun executeAllDeferred() {
        topDeferred?.invoke()
        topDeferred = null
    }

    public inline fun defer(crossinline block: Deferred) {
        var currentTop = topDeferred

        topDeferred = {
            try {
                block()
            } catch (first: Throwable) {
                try {
                    val top = currentTop
                    currentTop = null
                    top?.invoke()
                } catch (second: Throwable) {
                    first.addSuppressed(second)
                }
                throw first
            } finally {
                currentTop?.invoke()
            }
        }
    }

    public fun <T : Closeable> T.useInScope(): T {
        defer { this.close() }
        return this
    }
}

public open class CloseableDeferScope : DeferScope(), Closeable {
    override fun close() { executeAllDeferred() }
}

public inline fun <T> deferScope(block: DeferScope.() -> T): T = CloseableDeferScope().use(block)
