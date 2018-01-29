// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml

class Ensemble : Predictor {
    val predictors: Array<Predictor?>

    private val size: Int
    private val targetSize: Int
    private val compose: (DoubleArray) -> DoubleArray

    constructor(size: Int, targetSize: Int, method: String) {
        predictors = arrayOfNulls(size)

        this.size = size
        this.targetSize = targetSize
        compose = when (method) {
            "sum" -> ::sum
            "avg" -> ::avg
            else -> throw Exception(
                "Unknown compose method"
            )
        }
    }

    override fun predict(values: DoubleArray) = compose(values)

    private fun sum(values: DoubleArray): DoubleArray {
        val scores = DoubleArray(targetSize, { 0.0 })
        for (t in 0 until size) if (predictors[t] != null) {
            val prediction = predictors[t]!!.predict(values)
            for (k in 0 until targetSize) scores[k] += prediction[k]
        }
        return scores
    }

    private fun avg(values: DoubleArray): DoubleArray {
        val scores = sum(values)
        val actualSize = predictors.count({ it != null })
        if (actualSize > 0) for (k in 0 until targetSize) {
            scores[k] = scores[k] / actualSize
        }
        return scores
    }
}
