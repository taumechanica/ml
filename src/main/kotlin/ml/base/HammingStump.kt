// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.base

import kotlin.math.*

import taumechanica.ml.Classifier
import taumechanica.ml.data.*

class HammingStump : Classifier {
    override val alpha: Double
    override val gamma: Double

    override val votes: DoubleArray

    val index: Int
    val value: DoubleArray

    constructor(frame: DataFrame, attr: Attribute, baseline: DoubleArray) {
        if (attr !is NumericAttribute) {
            throw Exception("Unexpected attribute type")
        }

        index = attr.index
        value = doubleArrayOf(0.0)
        votes = DoubleArray(frame.target.size, { 0.0 })

        val indices = mutableListOf<Int>()
        for (i in attr.order) if (frame.subset[i]) indices.add(i)

        var best = 0.0
        for (k in 0 until frame.target.size) {
            best += abs(baseline[k])
        }

        var i = 0
        var edge = baseline.copyOf()
        while (
            i < indices.size - 1 &&
            frame.samples[indices[i]].values[index] < attr.mostFrequent
        ) {
            val sample = frame.samples[indices[i]]
            for (k in 0 until frame.target.size) {
                edge[k] -= 2.0 * sample.weight[k] * sample.target[k]
            }

            if (sample.values[index] !=
                frame.samples[indices[i + 1]].values[index]
            ) {
                var candidate = 0.0
                for (k in 0 until frame.target.size) {
                    candidate += abs(edge[k])
                }

                if (candidate > best) {
                    best = candidate
                    for (k in 0 until frame.target.size) {
                        votes[k] = if (edge[k] > 0.0) 1.0 else -1.0
                    }
                    value[0] = 0.5 * (
                        sample.values[index] +
                        frame.samples[indices[i + 1]].values[index]
                    )
                }
            }

            i++
        }

        i = indices.size - 1
        edge = baseline.copyOf()
        while (i > 0 && frame.samples[indices[i]].values[index] > attr.mostFrequent) {
            val sample = frame.samples[indices[i]]
            for (k in 0 until frame.target.size) {
                edge[k] -= 2.0 * sample.weight[k] * sample.target[k]
            }

            if (sample.values[index] !=
                frame.samples[indices[i - 1]].values[index]
            ) {
                var candidate = 0.0
                for (k in 0 until frame.target.size) {
                    candidate += abs(edge[k])
                }

                if (candidate > best) {
                    best = candidate
                    for (k in 0 until frame.target.size) {
                        votes[k] = if (edge[k] < 0.0) 1.0 else -1.0
                    }
                    value[0] = 0.5 * (
                        sample.values[index] +
                        frame.samples[indices[i - 1]].values[index]
                    )
                }
            }

            i--
        }

        gamma = best
        alpha = 0.5 * ln((1.0 + gamma) / (1.0 - gamma))
    }

    override fun predict(values: DoubleArray): DoubleArray {
        val phi = phi(values)
        return DoubleArray(votes.size, { alpha * votes[it] * phi })
    }

    override fun phi(values: DoubleArray) = if (values[index] > value[0]) 1 else -1
}
