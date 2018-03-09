// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.meta

import java.util.PriorityQueue
import java.util.concurrent.ThreadLocalRandom

import taumechanica.ml.BinaryClassifier
import taumechanica.ml.base.*
import taumechanica.ml.data.*

class IsolationTree {
    val classifiers = arrayListOf<BinaryClassifier>()
    val lIndices = arrayListOf<Int>()
    val rIndices = arrayListOf<Int>()

    constructor(frame: DataFrame, complexity: Int) {
        if (complexity < 2) throw Exception(
            "Tree complexity should be greater or equals 2"
        )

        val queue = PriorityQueue<ITQueueItem>(1, compareBy({ it.priority }))
        queue.add(ITQueueItem(generate(frame), frame, 1.0, -1, 0))

        var iteration = 0
        while (true) {
            val item = queue.poll()
            if (item.priority == 0.0) break

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

            val (neg, pos) = item.frame.cut(item.classifier)
            for (direction in intArrayOf(-1, 1)) {
                val cut = if (direction < 0) neg else pos

                var cardinality = 0.0
                for (i in 0 until cut.samples.size) if (cut.subset[i]) cardinality++

                val priority = cardinality / frame.samples.size
                queue.add(ITQueueItem(generate(cut), cut, priority, iteration, direction))
            }

            iteration++
        }
    }

    fun score(values: DoubleArray): Double {
        var index = 0
        var path = 1.0
        while (true) {
            val classifier = classifiers[index]
            val phi = classifier.phi(values)
            if (phi < 0) {
                if (lIndices[index] > -1) {
                    index = lIndices[index]
                } else {
                    return path
                }
            }
            if (phi > 0) {
                if (rIndices[index] > -1) {
                    index = rIndices[index]
                } else {
                    return path
                }
            }
            path++
        }
    }
}

private class ITQueueItem(
    val classifier: BinaryClassifier,
    val frame: DataFrame,
    val priority: Double,
    val parent: Int,
    val direction: Int
)

private fun generate(frame: DataFrame): BinaryClassifier {
    var j = ThreadLocalRandom.current().nextInt(frame.features.size)
    return when (frame.features[j]) {
        is NominalAttribute -> RandomIndicator(frame.features[j])
        is NumericAttribute -> IsolationStump(frame, frame.features[j])
        else -> null
    }!!
}
