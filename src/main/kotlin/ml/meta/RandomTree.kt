// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.meta

import java.util.ArrayDeque
import java.util.concurrent.ThreadLocalRandom

import kotlin.math.*

import taumechanica.ml.BinaryClassifier
import taumechanica.ml.base.*
import taumechanica.ml.data.*

class RandomTree {
    val classifiers = arrayListOf<BinaryClassifier>()

    val lIndices = arrayListOf<Int>()
    val rIndices = arrayListOf<Int>()

    val complexity: Int
    val lShift: Double
    val rShift: Double

    constructor(frame: DataFrame, complexity: Int) {
        if (complexity < 1) throw Exception(
            "Tree complexity should be greater or equals 1"
        )

        var queue = ArrayDeque<RTQueueItem>()
        queue.add(RTQueueItem(generate(frame), -1, 0))

        var iteration = 0
        while (true) {
            val item = queue.poll()

            lIndices.add(-1)
            rIndices.add(-1)
            if (item.direction < 0) {
                lIndices[item.parent] = iteration
            }
            if (item.direction > 0) {
                rIndices[item.parent] = iteration
            }

            classifiers.add(item.classifier)

            if (iteration == complexity - 1) break

            for (direction in intArrayOf(-1, 1)) {
                queue.add(RTQueueItem(generate(frame), iteration, direction))
            }

            iteration++
        }

        this.complexity = complexity
        lShift = ceil((complexity - 1) / 2.0)
        rShift = floor((complexity - 1) / 2.0)
    }

    fun encode(values: DoubleArray): Double {
        var index = 0
        while (true) {
            val classifier = classifiers[index]
            val phi = classifier.phi(values)
            if (phi < 0) {
                if (lIndices[index] > -1) {
                    index = lIndices[index]
                } else {
                    return index - lShift
                }
            }
            if (phi > 0) {
                if (rIndices[index] > -1) {
                    index = rIndices[index]
                } else {
                    return complexity - lShift + index - rShift
                }
            }
        }
    }
}

private class RTQueueItem(
    val classifier: BinaryClassifier,
    val parent: Int,
    val direction: Int
)

private fun generate(frame: DataFrame): BinaryClassifier {
    var j = ThreadLocalRandom.current().nextInt(frame.features.size)
    return when (frame.features[j]) {
        is NominalAttribute -> RandomIndicator(frame.features[j])
        is NumericAttribute -> RandomStump(frame.features[j])
        else -> null
    }!!
}
