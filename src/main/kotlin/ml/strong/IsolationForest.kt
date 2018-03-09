// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.strong

import taumechanica.ml.data.DataFrame
import taumechanica.ml.meta.IsolationTree

class IsolationForest {
    val trees: Array<IsolationTree>

    constructor(frame: DataFrame, size: Int, complexity: Int) {
        trees = Array<IsolationTree>(size, { IsolationTree(frame, complexity) })
    }

    fun score(values: DoubleArray) = DoubleArray(
        trees.size, { trees[it].score(values) }
    ).average()
}
