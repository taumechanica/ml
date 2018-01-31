// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml

interface Predictor {
    fun predict(values: DoubleArray): DoubleArray
}

interface BinaryClassifier {
    fun phi(values: DoubleArray): Int
}

interface Classifier : Predictor, BinaryClassifier {
    val alpha: Double
    val gamma: Double

    val votes: DoubleArray
}
