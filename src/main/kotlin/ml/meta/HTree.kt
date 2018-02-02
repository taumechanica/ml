// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.meta

import java.util.PriorityQueue

import kotlin.math.ln

import taumechanica.ml.*
import taumechanica.ml.data.DataFrame

class HTree : Classifier {
    override val alpha: Double
    override val gamma: Double

    override val votes: DoubleArray

    val classifiers = arrayListOf<Classifier>()
    val lIndices = arrayListOf<Int>()
    val rIndices = arrayListOf<Int>()

    constructor(frame: DataFrame, strategy: Strategy, complexity: Int) {
        if (complexity < 2) throw Exception(
            "Tree complexity should be greater or equals 2"
        )

        votes = DoubleArray(frame.target.size, { 0.0 })

        val queue = PriorityQueue<HQueueItem>(0, compareBy({ it.priority }))

        var classifier = strategy.fit(frame)
        queue.add(HQueueItem(classifier, frame, classifier.gamma, -1, 0))

        var iteration = 0
        var result = 0.0
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
            result += item.priority

            if (iteration == complexity - 1) break

            val (neg, pos) = item.frame.cut(item.classifier)
            for (direction in intArrayOf(-1, 1)) {
                val cut = if (direction < 0) neg else pos
                classifier = strategy.fit(cut)

                val priority = classifier.gamma - edge(cut, item.classifier)
                queue.add(HQueueItem(classifier, cut, priority, iteration, direction))
            }

            iteration++
        }

        gamma = result
        alpha = 0.5 * ln((1.0 + gamma) / (1.0 - gamma))
    }

    override fun predict(values: DoubleArray): DoubleArray {
        var index = 0
        while (true) {
            val classifier = classifiers[index]
            val phi = classifier.phi(values)
            if (phi < 0) {
                if (lIndices[index] == -1) {
                    val v = classifier.votes
                    return DoubleArray(v.size, { -alpha * v[it] })
                } else {
                    index = lIndices[index]
                }
            }
            if (phi > 0) {
                if (rIndices[index] == -1) {
                    val v = classifier.votes
                    return DoubleArray(v.size, { alpha * v[it] })
                } else {
                    index = rIndices[index]
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    override fun phi(values: DoubleArray) = 0
}

private class HQueueItem(
    val classifier: Classifier,
    val frame: DataFrame,
    val priority: Double,
    val parent: Int,
    val direction: Int
)

private fun edge(frame: DataFrame, classifier: Classifier): Double {
    var result = 0.0
    for (i in 0 until frame.samples.size) if (frame.subset[i]) {
        val sample = frame.samples[i]
        val phi = classifier.phi(sample.values)
        for (k in 0 until frame.target.size) {
            result += sample.weight[k] * classifier.votes[k] * phi * sample.target[k]
        }
    }
    return result
}
