// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.meta

import java.util.PriorityQueue

import kotlin.math.ln

import taumechanica.ml.*
import taumechanica.ml.data.DataFrame

private class QueueItem(
    val classifier: Classifier,
    val frame: DataFrame,
    val priority: Double,
    val parent: Int,
    val direction: Int
)

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

        val queue = PriorityQueue<QueueItem>(0, compareBy({ it.priority }))

        var classifier = strategy.fit(frame)
        queue.add(QueueItem(classifier, frame, classifier.gamma, -1, 0))

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
                classifier = strategy.fit(cut.first)

                val priority = classifier.gamma - cut.second
                queue.add(QueueItem(classifier, cut.first, priority, iteration, direction))
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
