// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.data

import java.util.Collections.shuffle

import kotlin.math.*

import taumechanica.ml.BinaryClassifier

class Sample(val values: DoubleArray) {
    lateinit var actual: DoubleArray
    lateinit var target: DoubleArray
    lateinit var weight: DoubleArray
}

class DataFrame(
    val target: Attribute,
    val features: Array<Attribute>,
    val samples: Array<Sample>,
    val subset: BooleanArray
) {
    fun initialize() {
        for (sample in samples) {
            if (target is NominalAttribute) {
                val value = sample.values[target.index]
                sample.actual = DoubleArray(target.size, {
                    if (value == target.domain[it]) 1.0 else -1.0
                })
            } else {
                sample.actual = doubleArrayOf(sample.values[target.index])
            }
            sample.target = sample.actual.copyOf()
            sample.weight = DoubleArray(target.size)
        }
    }

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
        for (i in 0 until indices.size) {
            if (i < size) {
                second.subset[indices[i]] = false
            } else {
                first.subset[indices[i]] = false
            }
        }

        return Pair(first, second)
    }

    fun cut(classifier: BinaryClassifier): Pair<DataFrame, DataFrame> {
        val first = DataFrame(target, features, samples, subset.copyOf())
        val second = DataFrame(target, features, samples, subset.copyOf())

        for (i in 0 until samples.size) if (subset[i]) {
            val sample = samples[i]
            if (classifier.phi(sample.values) > 0) {
                first.subset[i] = false
            } else {
                second.subset[i] = false
            }
        }

        return Pair(first, second)
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

    fun distance(first: DoubleArray, second: DoubleArray): Double {
        var sum = 0.0
        for (feature in features) {
            sum += when (feature) {
                is NominalAttribute -> if (first[feature.index] == second[feature.index]) 0.0 else 1.0
                is NumericAttribute -> abs(first[feature.index] - second[feature.index]) /
                    if (feature.valueRange == 0.0) 1.0 else feature.valueRange
                else -> 0.0
            }
        }
        return sum
    }
}
