// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.instance

import taumechanica.ml.data.DataFrame

fun kNN(frame: DataFrame, values: DoubleArray, k: Int): List<Int> {
    if (k < 1) throw Exception("k should be greater or equals 1")

    val indices = mutableListOf<Pair<Int, Double>>()
    for (i in 0 until frame.samples.size) if (frame.subset[i]) {
        indices.add(i to frame.distance(frame.samples[i].values, values))
    }
    return indices.sortedBy { it.second }.take(k).map { it.first }
}
