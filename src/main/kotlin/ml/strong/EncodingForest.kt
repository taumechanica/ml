// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.strong

import taumechanica.ml.data.DataFrame
import taumechanica.ml.meta.RandomTree

class EncodingForest {
    val encoders: Array<RandomTree>

    constructor(
        frame: DataFrame,
        complexity: Int,
        extract: () -> IntArray?,
        size: Int = 0
    ) {
        encoders = if (size > 0) {
            val indices = IntArray(frame.features.size, { it })
            Array<RandomTree>(size, { RandomTree(frame, indices, complexity) })
        } else {
            var trees = mutableListOf<RandomTree>()
            var indices = extract()
            while (indices != null) {
                trees.add(RandomTree(frame, indices, complexity))
                indices = extract()
            }
            trees.toTypedArray()
        }
    }

    fun encode(values: DoubleArray, targetIndex: Int) = DoubleArray(
        encoders.size + 1, {
            if (it < encoders.size) {
                encoders[it].encode(values)
            } else {
                values[targetIndex]
            }
        }
    )
}
