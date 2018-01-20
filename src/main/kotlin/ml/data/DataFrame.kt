// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.data

class Sample(val values: DoubleArray) {
    var weight: DoubleArray? = null
    var actual: DoubleArray? = null
    var target: DoubleArray? = null
}

class DataFrame(
    val target: Attribute,
    val features: Array<Attribute>,
    val samples: Array<Sample>,
    val subset: BooleanArray,
    val filter: BooleanArray
) {
    
}
