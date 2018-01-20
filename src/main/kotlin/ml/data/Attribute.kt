// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.data

abstract class Attribute(
    val index: Int,
    val name: String,
    var size: Int = 1,
    var weight: Double = 0.0
)

class NominalAttribute(
    index: Int,
    name: String,
    var domain: DoubleArray? = null
) : Attribute(index, name)

class NumericAttribute(
    index: Int,
    name: String,
    var order: IntArray? = null,
    var mostFrequent: Double = 0.0
) : Attribute(index, name)
