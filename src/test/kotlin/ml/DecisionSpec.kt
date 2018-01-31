// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.test

import java.util.Locale

import kotlin.test.*

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

import taumechanica.ml.*

object DecisionSpec : Spek({
    Locale.setDefault(Locale.US)

    on("argmax") {
        it("should return target value with the maximum score") {
            assertEquals(argmax(doubleArrayOf(0.0, 1.0, 2.0), doubleArrayOf(0.0, -1.0, 0.1)), 2.0)
            assertEquals(argmax(doubleArrayOf(0.0, 1.0, 2.0), doubleArrayOf(-0.1, 1.0, 0.1)), 1.0)
        }
    }

    on("prob") {
        it("should return probability vector") {
            val p = prob(doubleArrayOf(1.0, -1.0, 0.0))
            assertTrue(DoubleArray(p.size, { p[it] - p[it] % 0.01 }) contentEquals doubleArrayOf(0.58, 0.07, 0.33))
            assertEquals("%.2f".format(p.sum()), "1.00")
        }
    }
})
