// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.strong

import kotlin.math.*

import taumechanica.ml.*
import taumechanica.ml.data.DataFrame
import taumechanica.ml.meta.HammingTree

class AdaBoostMH : Ensemble {
    constructor(
        frame: DataFrame,
        size: Int,
        strategy: Strategy,
        meta: String? = null,
        complexity: Int = 0
    ) : super(size, frame.target.size, "sum") {
        if (size < 1) throw Exception(
            "Ensemble size should be greater or equals 1"
        )

        val fit: (DataFrame) -> Classifier = when (meta) {
            "tree" -> { df -> HammingTree(df, strategy, complexity) }
            else -> { df -> strategy.fit(df) }
        }

        strategy.init(frame)

        for (iteration in 0 until size - 1) {
            val classifier = fit(frame)
            val sum = sqrt(1.0 - classifier.gamma.pow(2.0))
            for (i in 0 until frame.samples.size) if (frame.subset[i]) {
                val sample = frame.samples[i]
                val prediction = classifier.predict(sample.values)
                for (k in 0 until frame.target.size) {
                    val error = sample.weight[k] * exp(-prediction[k] * sample.actual[k])
                    sample.weight[k] = error / sum
                }
            }
            predictors[iteration] = classifier
        }
        predictors[size - 1] = fit(frame)
    }
}
