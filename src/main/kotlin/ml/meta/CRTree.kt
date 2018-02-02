// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.meta

import java.util.PriorityQueue
import java.util.concurrent.ThreadLocalRandom

import kotlin.math.abs

import taumechanica.ml.BinaryClassifier
import taumechanica.ml.base.*
import taumechanica.ml.data.*

class CRTree {
    val classifiers = arrayListOf<BinaryClassifier>()
    val lIndices = arrayListOf<Int>()
    val rIndices = arrayListOf<Int>()

    constructor(frame: DataFrame, complexity: Int) {
        if (complexity < 2) throw Exception(
            "Tree complexity should be greater or equals 2"
        )

        val queue = PriorityQueue<CRQueueItem>(0, compareBy({ it.priority }))

        var j = ThreadLocalRandom.current().nextInt(0, frame.features.size)
        var classifier = when (frame.features[j]) {
            is NominalAttribute -> CRIndicator(frame.features[j])
            is NumericAttribute -> CRStump(frame, frame.features[j])
            else -> null
        }!!
        queue.add(CRQueueItem(classifier, frame, diversity(frame), -1, 0))

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
                j = ThreadLocalRandom.current().nextInt(0, frame.features.size)
                classifier = when (frame.features[j]) {
                    is NominalAttribute -> CRIndicator(frame.features[j])
                    is NumericAttribute -> CRStump(cut, frame.features[j])
                    else -> null
                }!!
                queue.add(CRQueueItem(classifier, cut, diversity(cut), iteration, direction))
            }

            iteration++
        }
    }

    fun encode(values: DoubleArray): Int {
        var index = 0
        while (true) {
            val classifier = classifiers[index]
            val phi = classifier.phi(values)
            if (phi < 0) {
                if (lIndices[index] > -1) {
                    index = lIndices[index]
                } else return -index
            }
            if (phi > 0) {
                if (rIndices[index] > -1) {
                    index = rIndices[index]
                } else return index
            }
        }
    }
}

private class CRQueueItem(
    val classifier: BinaryClassifier,
    val frame: DataFrame,
    val priority: Double,
    val parent: Int,
    val direction: Int
)

private fun diversity(frame: DataFrame): Double {
    var size = 0.0
    val edge = DoubleArray(frame.target.size, { 0.0 })
    for (i in 0 until frame.samples.size) if (frame.subset[i]) {
        size++
        for (k in 0 until frame.target.size) {
            edge[k] += frame.samples[i].target[k]
        }
    }

    var sum = 0.0
    for (k in 0 until frame.target.size) sum += abs(edge[k])
    return if (sum == frame.target.size * size) 0.0
    else 1.0 - sum / frame.target.size / size
}
