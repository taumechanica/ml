// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.base

import java.util.concurrent.ThreadLocalRandom

import taumechanica.ml.BinaryClassifier
import taumechanica.ml.data.*

class IsolationStump : BinaryClassifier {
    val index: Int
    val value: DoubleArray

    constructor(frame: DataFrame, attr: Attribute) {
        if (attr !is NumericAttribute) {
            throw Exception("Unexpected attribute type")
        }

        var minValue = 0.0
        var maxValue = 0.0
        for (i in attr.order) if (frame.subset[i]) {
            minValue = frame.samples[i].values[attr.index]
            break
        }
        for (i in attr.order.size - 1 downTo 0) if (frame.subset[i]) {
            maxValue = frame.samples[i].values[attr.index]
            break
        }

        index = attr.index
        value = doubleArrayOf(
            if (maxValue > minValue) {
                ThreadLocalRandom.current().nextDouble(minValue, maxValue)
            } else minValue
        )
    }

    override fun phi(values: DoubleArray) = if (values[index] > value[0]) 1 else -1
}
