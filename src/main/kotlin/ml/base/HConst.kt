// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.base

import kotlin.math.*

import taumechanica.ml.Predictor
import taumechanica.ml.data.DataFrame

class HConst : Predictor {
    val alpha: Double
    val gamma: Double

    val edge: DoubleArray
    val votes: DoubleArray

    constructor(frame: DataFrame) {
        edge = DoubleArray(frame.target.size, { 0.0 })
        votes = DoubleArray(frame.target.size, { 0.0 })

        for (i in 0 until frame.samples.size) if (frame.subset[i]) {
            val sample = frame.samples[i]
            for (k in 0 until frame.target.size) {
                edge[k] += sample.weight[k] * sample.target[k]
            }
        }

        var sum = 0.0
        for (k in 0 until frame.target.size) {
            sum += abs(edge[k])
            votes[k] = if (edge[k] > 0.0) 1.0 else -1.0
        }

        gamma = sum
        alpha = 0.5 * ln((1.0 + gamma) / (1.0 - gamma))
    }

    override fun predict(values: DoubleArray): DoubleArray {
        return DoubleArray(votes.size, { alpha * votes[it] })
    }

    @Suppress("UNUSED_PARAMETER")
    fun phi(values: DoubleArray) = 1
}
