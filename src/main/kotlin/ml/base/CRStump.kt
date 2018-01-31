// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.base

import java.util.concurrent.ThreadLocalRandom

import taumechanica.ml.BinaryClassifier
import taumechanica.ml.data.*

class CRStump : BinaryClassifier {
    val index: Int
    val value: DoubleArray

    constructor(frame: DataFrame, attr: Attribute) {
        if (attr !is NumericAttribute) {
            throw Exception("Unexpected attribute type")
        }

        index = attr.index
        value = doubleArrayOf(0.0)

        val indices = mutableListOf<Int>()
        for (i in attr.order) if (frame.subset[i]) indices.add(i)

        val i = ThreadLocalRandom.current().nextInt(0, indices.size)
        value[0] = frame.samples[indices[i]].values[index]
    }

    override fun phi(values: DoubleArray) = if (values[index] > value[0]) 1 else -1
}
