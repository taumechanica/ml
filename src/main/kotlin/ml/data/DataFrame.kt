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
    val subset: BooleanArray,
    val filter: BooleanArray?
) {
    public fun split(ratio: Double): Pair<DataFrame, DataFrame> {
        if (ratio <= 0.0 || ratio > 1.0) {
            throw Exception("Invalid split ratio")
        }

        val indices = mutableListOf<Int>()
        for (i in 0 until samples.size) if (subset[i]) indices.add(i)

        val permutation = shuffle(indices)
        val first = DataFrame(target, features, samples, subset.copyOf(), null)
        val second = DataFrame(target, features, samples, subset.copyOf(), null)
        val size = Math.ceil(indices.size * ratio).toInt()
        for (i in indices.size - size + 1 until indices.size) first.subset[i] = false
        for (i in 0 until size) second.subset[i] = false

        return Pair(first, second)
    }
}
