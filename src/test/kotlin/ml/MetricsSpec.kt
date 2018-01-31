// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.test

import java.io.ByteArrayInputStream
import java.util.Locale

import kotlin.test.*

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

import taumechanica.ml.*
import taumechanica.ml.data.ArffM

object MetricsSpec : Spek({
    Locale.setDefault(Locale.US)

    class Model : Predictor {
        override fun predict(values: DoubleArray) = doubleArrayOf(0.6, 0.2)
    }

    val source = ByteArrayInputStream(MockData.twoValueTarget.toByteArray())
    val frame = ArffM(";").parse(source)
    val model = Model()

    on("accuracy") {
        it("should calculate model accuracy against test subset") {
            assertEquals(accuracy(frame, Model()), 60.0)
        }
    }

    on("logloss") {
        it("should calculate logarithmic loss against test subset") {
            assertEquals("%.4f".format(logloss(frame, model)), "0.6759")
        }
    }
})
