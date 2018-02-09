// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml

import java.io.InputStream

import taumechanica.ml.data.*
import taumechanica.ml.strong.CRForest

class Pipeline {
    var frame: DataFrame? = null
    var encoders: MutableList<CRForest>? = null

    fun parse(source: InputStream, separator: String = ";"): Pipeline {
        if (frame != null) throw Exception(
            "Data frame has been already initialized"
        )

        frame = ArffM(separator).parse(source)

        return this
    }

    fun encode(size: Int, complexity: Int): Pipeline {
        if (frame == null) throw Exception(
            "Data frame has not been initialized"
        )

        if (encoders == null) {
            encoders = mutableListOf<CRForest>()
        }

        val features = Array<NominalAttribute>(size, {
            NominalAttribute(it, it.toString())
        })
        for (feature in features) {
            feature.domain = DoubleArray(complexity + 1, { it.toDouble() })
            feature.size = complexity + 1
        }

        val encoder = CRForest(frame!!, size, complexity)
        val samples = frame!!.samples.map { sample -> Sample(
            encoder.encode(sample.values, frame!!.target.index)
        ) }

        @Suppress("UNCHECKED_CAST")
        frame = DataFrame(
            frame!!.target,
            features as Array<Attribute>,
            samples.toTypedArray(),
            BooleanArray(samples.size, { true })
        )
        frame!!.target.index = size
        encoders!!.add(encoder)

        return this
    }
}
