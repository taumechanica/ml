// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.data

import java.io.InputStream
import java.util.Scanner

class ArffM(val separator: String) {
    private var section = "attributes"

    private lateinit var targetAttr: Attribute

    private var attrCounter = -1
    private val attrIndices = mutableMapOf<Int, Int>()
    private val features = mutableListOf<Attribute>()
    private val encoding = mutableMapOf<Int, MutableMap<String, Double>>()
    private val samples = mutableListOf<Sample>()

    private fun parseLine(line: String) {
        if (line.isEmpty() ||
            line.startsWith("@rem")
        ) return

        when (section) {
            "attributes" -> parseAttribute(line)
            "data" -> parseData(line)
        }
    }

    private fun parseAttribute(line: String) {
        if (line.startsWith("@data")) {
            section = "data"
            return
        }

        if (!line.startsWith("@attribute ")) {
            throw Exception("Bad format: @attribute expected")
        }

        val parts = line.removePrefix("@attribute ").split(" ")
        val isTarget = parts.size > 2 && parts[2] == "target"
        if (isTarget && ::targetAttr.isInitialized) {
            throw Exception("Multiple targets does not supported")
        }

        attrCounter++

        var index = features.size
        if (::targetAttr.isInitialized) index += 1

        val attr = when (parts[1]) {
            "nominal" -> NominalAttribute(index, parts[0])
            "numeric" -> NumericAttribute(index, parts[0])
            else -> null
        }

        if (attr != null) {
            if (isTarget) targetAttr = attr
            else features.add(attr)
            attrIndices[index] = attrCounter
        }
    }

    private fun parseData(line: String) {
        val parts = line.split(separator)
        val values = DoubleArray(features.size + 1)
        for (j in 0 until features.size) {
            val index = features[j].index
            values[index] = when (features[j]) {
                is NominalAttribute -> encode(index, parts[attrIndices[index] as Int])
                else -> {
                    val value = parts[attrIndices[index] as Int].trim()
                    if (value.isEmpty()) Double.MAX_VALUE else value.toDouble()
                }
            }
        }

        values[targetAttr.index] = if (targetAttr is NominalAttribute) {
            encode(targetAttr.index, parts[attrIndices[targetAttr.index] as Int])
        } else {
            parts[attrIndices[targetAttr.index] as Int].toDouble()
        }

        samples.add(Sample(values))
    }

    private fun encode(index: Int, value: String): Double {
        if (!encoding.containsKey(index)) {
            encoding[index] = mutableMapOf<String, Double>()
        }

        val map = encoding[index]
        if (map!!.containsKey(value)) return map[value] as Double

        map[value] = map.size.toDouble()
        return map[value] as Double
    }

    private fun fillDomain(attr: NominalAttribute) {
        val unique = samples.map { s -> s.values[attr.index] }.distinct()
        val domain = DoubleArray(unique.size)
        for ((index, value) in unique.withIndex()) domain[index] = value
        attr.domain = domain
        attr.size = domain.size
    }

    private fun fillOrder(attr: NumericAttribute) {
        val values = samples
            .map { s -> s.values[attr.index] }
            .withIndex().sortedBy { it.value }
        val order = IntArray(values.size)
        for ((index, pair) in values.withIndex()) order[index] = pair.index

        var bestCount = 0
        var bestCandidate = 0.0
        var currCount = 1
        var currCandidate = samples[order[0]].values[attr.index]
        for (i in 1 until order.size) {
            val value = samples[order[i]].values[attr.index]
            if (value != currCandidate) {
                if (currCount > bestCount) {
                    bestCount = currCount
                    bestCandidate = currCandidate
                }
                currCount = 1
                currCandidate = value
            } else currCount++
        }
        if (currCount > bestCount) {
            bestCandidate = currCandidate
        }

        attr.order = order
        attr.minValue = samples[order[0]].values[attr.index]
        attr.maxValue = samples[order[order.size - 1]].values[attr.index]
        attr.mostFrequent = bestCandidate
    }

    fun parse(source: InputStream): DataFrame {
        val scanner = Scanner(source)
        while (scanner.hasNextLine()) {
            parseLine(scanner.nextLine().trim())
        }

        if (!::targetAttr.isInitialized) {
            throw Exception("Target attribute not found")
        }

        for (feature in features) when (feature) {
            is NominalAttribute -> fillDomain(feature)
            is NumericAttribute -> fillOrder(feature)
        }

        if (targetAttr is NominalAttribute) {
            fillDomain(targetAttr as NominalAttribute)
        }

        val frame = DataFrame(
            targetAttr,
            features.toTypedArray(),
            samples.toTypedArray(),
            BooleanArray(samples.size, { true })
        )
        frame.initialize()
        return frame
    }
}
