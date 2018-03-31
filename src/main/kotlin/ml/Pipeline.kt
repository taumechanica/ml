// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml

import java.io.InputStream

import taumechanica.ml.data.*
import taumechanica.ml.strong.EncodingForest

class Pipeline {
    var frame: DataFrame? = null
    var encoders: MutableList<EncodingForest>? = null

    fun parse(source: InputStream, separator: String = ";"): Pipeline {
        if (frame != null) throw Exception(
            "Data frame has been already initialized"
        )

        frame = ArffM(separator).parse(source)

        return this
    }

    fun encode(complexity: Int, extract: () -> IntArray?, size: Int = 0): Pipeline {
        if (frame == null) throw Exception(
            "Data frame has not been initialized"
        )

        if (encoders == null) {
            encoders = mutableListOf<EncodingForest>()
        }

        val forest = EncodingForest(frame!!, complexity, extract, size)
        val actualSize = forest.encoders.size

        val samples = frame!!.samples.map { sample -> Sample(
            forest.encode(sample.values, frame!!.target.index)
        ) }
        frame!!.target.index = actualSize

        val features = Array<NominalAttribute>(actualSize, {
            NominalAttribute(it, it.toString())
        })
        for (feature in features) {
            feature.domain = DoubleArray(complexity + 1, { it.toDouble() })
            feature.size = complexity + 1
        }

        @Suppress("UNCHECKED_CAST")
        frame = DataFrame(
            frame!!.target,
            features as Array<Attribute>,
            samples.toTypedArray(),
            BooleanArray(samples.size, { true })
        )
        frame!!.initialize()
        encoders!!.add(forest)

        return this
    }
}
