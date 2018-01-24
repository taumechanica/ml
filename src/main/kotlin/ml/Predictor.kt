// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml

interface Predictor {
    fun predict(values: DoubleArray): DoubleArray
}
