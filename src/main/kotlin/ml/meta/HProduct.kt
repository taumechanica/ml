// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.meta

import taumechanica.ml.*
import taumechanica.ml.base.HUnit
import taumechanica.ml.data.DataFrame

class HProduct : Classifier {
    override val alpha: Double
    override val gamma: Double

    override val votes: DoubleArray

    val factors: Array<Classifier>

    constructor(frame: DataFrame, strategy: Strategy, complexity: Int) {
        if (complexity < 2) throw Exception(
            "Product complexity should be greater or equals 2"
        )

        votes = DoubleArray(frame.target.size, { 0.0 })
        factors = Array(complexity, { HUnit(frame) })

        frame.resetTargets()

        var j = 0
        var bestAlpha = 1.0
        var bestGamma = 0.0
        while (true) {
            for (k in 0 until frame.target.size) votes[k] = 1.0
            for (factor in factors) {
                for (k in 0 until frame.target.size) {
                    votes[k] *= factor.votes[k]
                }
            }

            for (i in 0 until frame.samples.size) if (frame.subset[i]) {
                val sample = frame.samples[i]
                val phi = phi(sample.values)
                val numerator = DoubleArray(votes.size, { bestAlpha * votes[it] * phi })
                val denominator = factors[j].predict(sample.values)
                for (k in 0 until frame.target.size) {
                    sample.target[k] = sample.actual[k] * (
                        if (numerator[k] / denominator[k] > 0) 1.0 else -1.0
                    )
                }
            }

            val factor = strategy.fit(frame)
            if (factor.gamma <= bestGamma) break

            bestAlpha = factor.alpha
            bestGamma = factor.gamma
            factors[j] = factor

            j = if (j == complexity - 1) 0 else j + 1
        }

        alpha = bestAlpha
        gamma = bestGamma
    }

    override fun predict(values: DoubleArray): DoubleArray {
        val phi = phi(values)
        return DoubleArray(votes.size, { alpha * votes[it] * phi })
    }

    override fun phi(values: DoubleArray): Int {
        return IntArray(factors.size, { factors[it].phi(values) }).reduce { prev, curr -> curr * prev }
    }
}
