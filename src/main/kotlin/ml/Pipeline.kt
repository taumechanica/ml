// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml

import java.io.InputStream

import taumechanica.ml.data.*

class Pipeline {
    var frame: DataFrame? = null

    fun parse(source: InputStream, separator: String = ";"): Pipeline {
        if (frame != null) throw Exception(
            "Data frame has been already initialized"
        )

        frame = ArffM(separator).parse(source)

        return this
    }
}
