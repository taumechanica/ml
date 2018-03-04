// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.data

abstract class Attribute(var index: Int, val name: String) {
    var size = 1
    var weight = 0.0
}

class NominalAttribute(index: Int, name: String) : Attribute(index, name) {
    lateinit var domain: DoubleArray
}

class NumericAttribute(index: Int, name: String) : Attribute(index, name) {
    lateinit var order: IntArray
    var minValue = 0.0
    var maxValue = 0.0
    var valueRange = 0.0
    var mostFrequent = 0.0
}
