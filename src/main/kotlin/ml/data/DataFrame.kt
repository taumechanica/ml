// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.data

import java.util.Collections.shuffle

import kotlin.math.*

import taumechanica.ml.Classifier

class Sample(val values: DoubleArray) {
    lateinit var weight: DoubleArray
    lateinit var actual: DoubleArray
    lateinit var target: DoubleArray
}

class DataFrame(
    val target: Attribute,
    val features: Array<Attribute>,
    val samples: Array<Sample>,
    val subset: BooleanArray
) {
    fun split(ratio: Double): Pair<DataFrame, DataFrame> {
        if (ratio <= 0.0 || ratio > 1.0) {
            throw Exception("Invalid split ratio")
        }

        val indices = mutableListOf<Int>()
        for (i in 0 until samples.size) {
            if (subset[i]) indices.add(i)
        }
        shuffle(indices)

        val first = DataFrame(target, features, samples, subset.copyOf())
        val second = DataFrame(target, features, samples, subset.copyOf())
        val size = ceil(indices.size * ratio).toInt()
        for (i in indices.size - size + 1 until indices.size) first.subset[i] = false
        for (i in 0 until size) second.subset[i] = false

        return Pair(first, second)
    }

    fun cut(classifier: Classifier): Pair<
        Pair<DataFrame, Double>,
        Pair<DataFrame, Double>
    > {
        var firstEdge = 0.0
        var secondEdge = 0.0

        val first = DataFrame(target, features, samples, subset.copyOf())
        val second = DataFrame(target, features, samples, subset.copyOf())

        for (i in 0 until samples.size) if (subset[i]) {
            val sample = samples[i]
            val phi = classifier.phi(sample.values)
            if (phi > 0) {
                first.subset[i] = false
                for (k in 0 until target.size) {
                    secondEdge += sample.weight[k] * classifier.votes[k] * sample.target[k]
                }
            } else {
                second.subset[i] = false
                for (k in 0 until target.size) {
                    firstEdge -= sample.weight[k] * classifier.votes[k] * sample.target[k]
                }
            }
        }

        return Pair(
            Pair(first, firstEdge),
            Pair(second, secondEdge)
        )
    }

    fun weighSamples() {
        val size = subset.filter({ it }).size
        for (i in 0 until samples.size) if (subset[i]) {
            for (k in 0 until target.size) {
                val sample = samples[i]
                if (sample.actual[k] > 0.0) {
                    sample.weight[k] = 1.0 / (2.0 * size)
                } else {
                    sample.weight[k] = 1.0 / (2.0 * (size * (target.size - 1.0)))
                }
            }
        }
    }

    fun weighFeatures(size: Int, eta: Double, lambda: Double) {
        val m = features.size
        for (feature in features) feature.weight = exp(
            eta * lambda / 3.0 * sqrt(size.toDouble() / m)
        )
    }

    fun resetTargets() {
        for (i in 0 until samples.size) if (subset[i]) {
            for (k in 0 until target.size) {
                samples[i].target[k] = samples[i].actual[k]
            }
        }
    }
}
