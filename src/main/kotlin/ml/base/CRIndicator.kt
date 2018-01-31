// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.base

import java.util.Random

import taumechanica.ml.BinaryClassifier
import taumechanica.ml.data.*

class CRIndicator : BinaryClassifier {
    val index: Int
    val value: DoubleArray

    constructor(attr: Attribute) {
        if (attr !is NominalAttribute) {
            throw Exception("Unexpected attribute type")
        }

        val domain = attr.domain
        val random = Random()

        index = attr.index
        value = DoubleArray(domain.size, {
            if (random.nextDouble() > 0.5) 1.0 else -1.0
        })
    }

    override fun phi(values: DoubleArray) = value[values[index].toInt()].toInt()
}
