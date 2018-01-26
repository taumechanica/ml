// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml

import taumechanica.ml.data.DataFrame

interface Strategy {
    fun init(frame: DataFrame)
    fun fit(frame: DataFrame): Classifier
}
