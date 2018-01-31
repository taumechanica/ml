// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.base

import taumechanica.ml.Classifier
import taumechanica.ml.data.DataFrame

class HUnit : Classifier {
    override val alpha = 0.0
    override val gamma = 0.0

    override val votes: DoubleArray

    constructor(frame: DataFrame) {
        votes = DoubleArray(frame.target.size, { 1.0 })
    }

    @Suppress("UNUSED_PARAMETER")
    override fun predict(values: DoubleArray) = votes

    @Suppress("UNUSED_PARAMETER")
    override fun phi(values: DoubleArray) = 1
}
