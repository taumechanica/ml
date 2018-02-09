// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.strong

import taumechanica.ml.data.DataFrame
import taumechanica.ml.meta.CRTree

class CRForest {
    val encoders: Array<CRTree>

    constructor(frame: DataFrame, size: Int, complexity: Int) {
        encoders = Array<CRTree>(size, { CRTree(frame, complexity) })
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
