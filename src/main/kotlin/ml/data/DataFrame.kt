// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.data

import java.util.Collections.shuffle

class Sample(val values: DoubleArray) {
    var weight: DoubleArray? = null
    var actual: DoubleArray? = null
    var target: DoubleArray? = null
}

class DataFrame(
    val target: Attribute,
    val features: Array<Attribute>,
    val samples: Array<Sample>,
    val subset: BooleanArray
) {
    public fun split(ratio: Double): Pair<DataFrame, DataFrame> {
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
        val size = Math.ceil(indices.size * ratio).toInt()
        for (i in indices.size - size + 1 until indices.size) first.subset[i] = false
        for (i in 0 until size) second.subset[i] = false

        return Pair(first, second)
    }

    public fun cut(phi: (DoubleArray) -> Int): Pair<DataFrame, DataFrame> {
        val first = DataFrame(target, features, samples, subset.copyOf())
        val second = DataFrame(target, features, samples, subset.copyOf())

        for (i in 0 until samples.size) if (subset[i]) {
            if (phi(samples[i].values) > 0) {
                first.subset[i] = false
            } else {
                second.subset[i] = false
            }
        }

        return Pair(first, second)
    }

    public fun weighSamples() {
        val size = subset.filter({ it }).size
        for (i in 0 until samples.size) if (subset[i]) {
            for (k in 0 until target.size) {
                val sample = samples[i]
                if (sample.actual!![k] > 0.0) {
                    sample.weight!![k] = 1.0 / (2.0 * size)
                } else {
                    sample.weight!![k] = 1.0 / (2.0 * (size * (target.size - 1.0)))
                }
            }
        }
    }

    public fun weighFeatures(t: Int, η: Double, λ: Double) {
        val m = features.size
        for (feature in features) feature.weight = Math.exp(
            η * λ / 3.0 * Math.sqrt(t.toDouble() / m)
        )
    }

    public fun resetTargets() {
        for (i in 0 until samples.size) if (subset[i]) {
            for (k in 0 until target.size) {
                samples[i].target!![k] = samples[i].actual!![k]
            }
        }
    }
}
