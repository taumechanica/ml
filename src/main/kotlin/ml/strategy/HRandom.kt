// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.strategy

import java.util.Collections.shuffle

import kotlin.math.ceil

import taumechanica.ml.*
import taumechanica.ml.base.*
import taumechanica.ml.data.*

class HRandom(val ratio: Double) : Strategy {
    override fun init(frame: DataFrame) {
        if (ratio <= 0.0 || ratio > 1.0) {
            throw Exception("Invalid subspace ratio")
        }
    }

    override fun fit(frame: DataFrame): Classifier {
        val hconst = HConst(frame)
        var result = hconst as Classifier

        val indices = mutableListOf<Int>()
        for (j in 0 until frame.features.size) indices.add(j)
        shuffle(indices)

        val size = ceil(indices.size * ratio).toInt()
        for (j in 0 until size) {
            val feature = frame.features[j]
            val candidate = when (feature) {
                is NominalAttribute -> HIndicator(frame, feature)
                is NumericAttribute -> HStump(frame, feature, hconst.edge)
                else -> null
            }
            candidate?.let {
                if (candidate.gamma > result.gamma) result = candidate
            }
        }

        return result
    }
}
