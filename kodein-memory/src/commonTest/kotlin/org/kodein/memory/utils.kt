package org.kodein.memory

import kotlin.math.absoluteValue
import kotlin.test.asserter

internal fun assertNear(expected: Float, actual: Float, precision: Float = 0.01f) = asserter.assertTrue({ "Expected <$expected>, actual <$actual>." }, (actual - expected).absoluteValue < precision)
