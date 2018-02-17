// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.test.data

import java.io.ByteArrayInputStream

import kotlin.test.*

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

import taumechanica.ml.BinaryClassifier
import taumechanica.ml.data.ArffM
import taumechanica.ml.test.MockData

object DataFrameSpec : Spek({
    val twoValueSource = ByteArrayInputStream(MockData.twoValueTarget.toByteArray())
    val threeValueSource = ByteArrayInputStream(MockData.threeValueTarget.toByteArray())
    val twoValueFrame = ArffM(";").parse(twoValueSource)
    val threeValueFrame = ArffM(",").parse(threeValueSource)

    class Model(val func: (DoubleArray) -> Int) : BinaryClassifier {
        override fun phi(values: DoubleArray) = func(values)
    }

    on("split") {
        it("should create two subframes by random permutation") {
            var fcounter = 0
            var scounter = 0
            var split = twoValueFrame.split(0.6)
            var first = split.first
            var second = split.second
            for (i in 0 until twoValueFrame.samples.size) {
                if (first.subset[i]) fcounter++
                if (second.subset[i]) scounter++
            }
            assertEquals(fcounter, 3)
            assertEquals(scounter, 2)

            fcounter = 0
            scounter = 0
            split = twoValueFrame.split(0.4)
            first = split.first
            second = split.second
            for (i in 0 until twoValueFrame.samples.size) {
                if (first.subset[i]) fcounter++
                if (second.subset[i]) scounter++
            }
            assertEquals(fcounter, 2)
            assertEquals(scounter, 3)

            fcounter = 0
            scounter = 0
            split = twoValueFrame.split(1.0)
            first = split.first
            second = split.second
            for (i in 0 until twoValueFrame.samples.size) {
                if (first.subset[i]) fcounter++
                if (second.subset[i]) scounter++
            }
            assertEquals(fcounter, 5)
            assertEquals(scounter, 0)
        }
    }

    on("cut") {
        it("should create two subframes by separation function") {
            var fcounter = 0
            var scounter = 0
            var cut = twoValueFrame.cut(Model({ if (it[0] > 1.0) 1 else -1 }))
            var first = cut.first
            var second = cut.second
            for (i in 0 until twoValueFrame.samples.size) {
                if (first.subset[i]) fcounter++
                if (second.subset[i]) scounter++
            }
            assertEquals(fcounter, 3)
            assertEquals(scounter, 2)

            fcounter = 0
            scounter = 0
            cut = twoValueFrame.cut(Model({ if (it[2] == 0.0) 1 else -1 }))
            first = cut.first
            second = cut.second
            for (i in 0 until twoValueFrame.samples.size) {
                if (first.subset[i]) fcounter++
                if (second.subset[i]) scounter++
            }
            assertEquals(fcounter, 4)
            assertEquals(scounter, 1)
        }
    }

    on("weighSamples") {
        it("should init sample weights") {
            twoValueFrame.weighSamples()
            assertEquals("%.2f".format(twoValueFrame.samples.map { it.weight.sum() }.sum()), "1.00")

            threeValueFrame.weighSamples()
            assertEquals("%.2f".format(threeValueFrame.samples.map { it.weight.sum() }.sum()), "1.00")
        }
    }
})
