// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.base

import java.util.concurrent.ThreadLocalRandom

import kotlin.math.*

import taumechanica.ml.Classifier
import taumechanica.ml.data.*

class HammingIndicator : Classifier {
    override val alpha: Double
    override val gamma: Double

    override val votes: DoubleArray

    val index: Int
    val value: DoubleArray

    constructor(frame: DataFrame, attr: Attribute) {
        if (attr !is NominalAttribute) {
            throw Exception("Unexpected attribute type")
        }

        val domain = attr.domain
        val random = ThreadLocalRandom.current()

        index = attr.index
        value = DoubleArray(domain.size, {
            if (random.nextDouble() > 0.5) 1.0 else -1.0
        })
        votes = DoubleArray(frame.target.size, { 0.0 })

        val edge = DoubleArray(frame.target.size * domain.size, { 0.0 })
        for (i in 0 until frame.samples.size) if (frame.subset[i]) {
            val sample = frame.samples[i]
            for (k in 0 until frame.target.size) {
                edge[sample.values[index].toInt() * frame.target.size + k] += sample.weight[k] * sample.target[k]
            }
        }

        var best = 0.0
        while (true) {
            var candidate = 0.0
            for (k in 0 until frame.target.size) {
                var sum = 0.0
                for (j in 0 until domain.size) {
                    sum += edge[j * frame.target.size + k] * value[j]
                }
                votes[k] = if (sum > 0.0) 1.0 else -1.0
                candidate += abs(sum)
            }
            if (best >= candidate) break

            best = candidate
            candidate = 0.0
            for (j in 0 until domain.size) {
                var sum = 0.0
                for (k in 0 until frame.target.size) {
                    sum += edge[j * frame.target.size + k] * votes[k]
                }
                value[j] = if (sum > 0.0) 1.0 else -1.0
                candidate += abs(sum)
            }
            if (best >= candidate) break

            best = candidate
        }

        gamma = best
        alpha = 0.5 * ln((1.0 + gamma) / (1.0 - gamma))
    }

    override fun predict(values: DoubleArray): DoubleArray {
        val phi = phi(values)
        return DoubleArray(votes.size, { alpha * votes[it] * phi })
    }

    override fun phi(values: DoubleArray) = value[values[index].toInt()].toInt()
}
