// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.strategy

import java.util.concurrent.ThreadLocalRandom

import kotlin.math.*

import taumechanica.ml.*
import taumechanica.ml.base.*
import taumechanica.ml.data.*

class HammingExp3P(
    val size: Int,
    val eta: Double = 0.3,
    val lambda: Double = 0.15
) : Strategy {
    override fun init(frame: DataFrame) {
        if (eta <= 0.0 || lambda <= 0.0 || lambda > 1.0) {
            throw Exception("Unexpected smoothing parameters")
        }

        frame.weighFeatures(size, eta, lambda)
    }

    override fun fit(frame: DataFrame): Classifier {
        val hconst = HammingConst(frame)
        var result = hconst as Classifier

        var sum = 0.0
        val m = frame.features.size
        val indices = mutableListOf<Int>()
        for (j in 0 until m) {
            sum += frame.features[j].weight
            indices.add(j)
        }

        val cdf = DoubleArray(m + 1, { 0.0 })
        val p = DoubleArray(m, { (1.0 - lambda) * frame.features[it].weight / sum + lambda / m })

        sum = 0.0
        for (j in indices) {
            cdf[j + 1] = cdf[j] + p[j]
            sum += p[j]
        }

        var left = 0
        var right = m
        val r = ThreadLocalRandom.current().nextDouble() * sum
        while (right - left > 1) {
            val mid = (left + right) / 2
            if (cdf[mid] > r) right = mid
            else left = mid
        }

        val feature = frame.features[indices[left]]
        val candidate = when (feature) {
            is NominalAttribute -> HammingIndicator(frame, feature)
            is NumericAttribute -> HammingStump(frame, feature, hconst.edge)
            else -> null
        }
        candidate?.let {
            for (j in indices) {
                val reward = if (j == left) min(1.0, -ln(sqrt(1.0 - candidate.gamma.pow(2.0)))) / p[j] else 0.0
                frame.features[j].weight *= exp(lambda / 3.0 / m * (reward + eta / p[j] / sqrt(m.toDouble() * size)))
            }

            if (candidate.gamma > result.gamma) result = candidate
        }

        return result
    }
}
