// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.strategy

import taumechanica.ml.*
import taumechanica.ml.base.*
import taumechanica.ml.data.*

class HammingFull : Strategy {
    override fun init(frame: DataFrame) { }

    override fun fit(frame: DataFrame): Classifier {
        val hconst = HammingConst(frame)
        var result = hconst as Classifier

        for (feature in frame.features) {
            val candidate = when (feature) {
                is NominalAttribute -> HammingIndicator(frame, feature)
                is NumericAttribute -> HammingStump(frame, feature, hconst.edge)
                else -> null
            }
            candidate?.let {
                if (candidate.gamma > result.gamma) result = candidate
            }
        }

        return result
    }
}
