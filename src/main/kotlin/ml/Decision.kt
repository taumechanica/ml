// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml

import kotlin.math.exp

fun argmax(domain: DoubleArray, scores: DoubleArray): Double {
    return domain[scores.withIndex().maxBy { it.value }!!.index]
}

fun prob(scores: DoubleArray): DoubleArray {
    val probabilities = DoubleArray(scores.size, {
        1.0 / (1.0 + exp(-2.0 * scores[it]))
    })
    val sum = probabilities.sum()
    for (k in 0 until scores.size) {
        probabilities[k] /= sum
    }
    return probabilities
}
