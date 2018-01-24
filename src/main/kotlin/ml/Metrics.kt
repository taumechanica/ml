// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml

import taumechanica.ml.data.*

fun accuracy(frame: DataFrame, model: Predictor): Double {
    var total = 0
    var correct = 0
    val domain = (frame.target as NominalAttribute).domain
    for (i in 0 until frame.samples.size) if (frame.subset[i]) {
        val sample = frame.samples[i]
        val scores = model.predict(sample.values)
        val decision = argmax(domain!!, scores)
        if (decision == sample.values[frame.target.index]) correct++
        total++
    }
    return 100.0 * correct / total
}

fun logloss(frame: DataFrame, model: Predictor, calibrate: Boolean = true): Double {
    var total = 0
    var logloss = 0.0
    for (i in 0 until frame.samples.size) if (frame.subset[i]) {
        val sample = frame.samples[i]
        val scores = model.predict(sample.values)
        val probabilities = if (calibrate) prob(scores) else scores
        for ((k, p) in probabilities.withIndex()) {
            logloss += 0.5 * (sample.actual!![k] + 1.0) * Math.log(Math.max(Math.min(p, 1.0 - 1E-15), 1E-15))
        }
        total++
    }
    return logloss / -total
}

fun gini(frame: DataFrame, model: Predictor, calibrate: Boolean = true): Double {
    val domain = (frame.target as NominalAttribute).domain
    if (domain!!.size != 2) {
        throw Exception("Could not calculate")
    }

    val values = mutableListOf<DoubleArray>()
    for (i in 0 until frame.samples.size) if (frame.subset[i]) {
        val sample = frame.samples[i]
        val scores = model.predict(sample.values)
        val probabilities = if (calibrate) prob(scores) else scores
        values.add(doubleArrayOf(probabilities[0], 0.5 * (sample.actual!![0] + 1.0), i.toDouble()))
    }

    val g: (Int) -> Double = { by ->
        var losses = 0.0
        val sorted = values.sortedWith(compareBy({ -it[by] }, { it[2] }))
        for (row in sorted) losses += row[1]

        var cum = 0.0
        var sum = 0.0
        for (row in sorted) {
            cum += row[1] / losses
            sum += cum
        }

        sum -= (sorted.size + 1.0) / 2.0
        sum / sorted.size
    }

    return Math.max(0.0, g(0) / g(1))
}

fun auc(frame: DataFrame, model: Predictor, calibrate: Boolean = true): Double {
    return 0.5 * (gini(frame, model, calibrate) + 1.0)
}
