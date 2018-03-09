// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.base

import java.util.concurrent.ThreadLocalRandom

import taumechanica.ml.BinaryClassifier
import taumechanica.ml.data.*

class RandomStump : BinaryClassifier {
    val index: Int
    val value: DoubleArray

    constructor(attr: Attribute) {
        if (attr !is NumericAttribute) {
            throw Exception("Unexpected attribute type")
        }

        index = attr.index
        value = doubleArrayOf(
            ThreadLocalRandom.current().nextDouble(attr.minValue, attr.maxValue)
        )
    }

    override fun phi(values: DoubleArray) = if (values[index] > value[0]) 1 else -1
}
