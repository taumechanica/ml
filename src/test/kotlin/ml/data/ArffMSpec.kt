// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.test.data

import java.io.ByteArrayInputStream

import kotlin.test.*

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

import taumechanica.ml.data.*
import taumechanica.ml.test.MockData

object ArffMSpec : Spek({
    val source = ByteArrayInputStream(MockData.threeValueTarget.toByteArray())

    on("parse") {
        it("should parse input stream") {
            val frame = ArffM(",").parse(source)

            assertEquals(frame.features.size, 2)

            assertEquals(frame.features[0].name, "x1")
            assertEquals(frame.features[0].index, 0)
            assertEquals(frame.features[0]::class.simpleName, "NumericAttribute")

            val x1 = frame.features[0] as NumericAttribute
            assertTrue(x1.order contentEquals intArrayOf(3, 4, 0, 1, 5, 2))
            assertEquals(x1.minValue, 0.0)
            assertEquals(x1.maxValue, 2.1)
            assertEquals(x1.valueRange, 2.1)
            assertEquals(x1.mostFrequent, 1.6)

            assertEquals(frame.features[1].name, "x2")
            assertEquals(frame.features[1].index, 1)
            assertEquals(frame.features[1]::class.simpleName, "NominalAttribute")

            val x2 = frame.features[1] as NominalAttribute
            assertTrue(x2.domain contentEquals doubleArrayOf(0.0, 1.0, 2.0))

            assertEquals(frame.target.name, "y")
            assertEquals(frame.target.index, 2)
            assertEquals(frame.target.size, 3)
            assertEquals(frame.target::class.simpleName, "NominalAttribute")

            val y = frame.target as NominalAttribute
            assertTrue(y.domain contentEquals doubleArrayOf(0.0, 1.0, 2.0))

            assertEquals(frame.samples.size, 6)

            assertTrue(frame.samples[0].values contentEquals doubleArrayOf(1.0, 0.0, 0.0))
            assertTrue(frame.samples[0].actual contentEquals doubleArrayOf(1.0, -1.0, -1.0))
            assertTrue(frame.samples[0].target contentEquals doubleArrayOf(1.0, -1.0, -1.0))

            assertTrue(frame.samples[1].values contentEquals doubleArrayOf(1.6, 1.0, 1.0))
            assertTrue(frame.samples[1].actual contentEquals doubleArrayOf(-1.0, 1.0, -1.0))
            assertTrue(frame.samples[1].target contentEquals doubleArrayOf(-1.0, 1.0, -1.0))

            assertTrue(frame.samples[2].values contentEquals doubleArrayOf(2.1, 2.0, 2.0))
            assertTrue(frame.samples[2].actual contentEquals doubleArrayOf(-1.0, -1.0, 1.0))
            assertTrue(frame.samples[2].target contentEquals doubleArrayOf(-1.0, -1.0, 1.0))

            assertTrue(frame.samples[3].values contentEquals doubleArrayOf(0.0, 1.0, 1.0))
            assertTrue(frame.samples[3].actual contentEquals doubleArrayOf(-1.0, 1.0, -1.0))
            assertTrue(frame.samples[3].target contentEquals doubleArrayOf(-1.0, 1.0, -1.0))

            assertTrue(frame.samples[4].values contentEquals doubleArrayOf(0.4, 2.0, 0.0))
            assertTrue(frame.samples[4].actual contentEquals doubleArrayOf(1.0, -1.0, -1.0))
            assertTrue(frame.samples[4].target contentEquals doubleArrayOf(1.0, -1.0, -1.0))

            assertTrue(frame.samples[5].values contentEquals doubleArrayOf(1.6, 0.0, 0.0))
            assertTrue(frame.samples[5].actual contentEquals doubleArrayOf(1.0, -1.0, -1.0))
            assertTrue(frame.samples[5].target contentEquals doubleArrayOf(1.0, -1.0, -1.0))
        }
    }
})
