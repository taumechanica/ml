// Use of this source code is governed by The MIT License
// that can be found in the LICENSE file.

package taumechanica.ml.data

import java.io.InputStream
import java.util.Scanner

class ArffM(val separator: String) {
    private var section: String? = null
    private var targetAttr: Attribute? = null
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
        if (isTarget && targetAttr != null) {
            throw Exception("Multiple targets does not supported")
        }

        attrCounter++

        var index = features.size
        if (targetAttr != null) index += 1

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

        targetAttr?.let {
            values[it.index] = if (it is NominalAttribute) {
                encode(it.index, parts[attrIndices[it.index] as Int])
            } else {
                parts[attrIndices[it.index] as Int].toDouble()
            }
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
        attr.mostFrequent = bestCandidate
    }

    public fun parse(source: InputStream): DataFrame {
        section = "attributes"

        val scanner = Scanner(source)
        while (scanner.hasNextLine()) {
            parseLine(scanner.nextLine().trim())
        }

        if (targetAttr == null) {
            throw Exception("Target attribute not found")
        }

        for (feature in features) when (feature) {
            is NominalAttribute -> fillDomain(feature)
            is NumericAttribute -> fillOrder(feature)
        }

        if (targetAttr is NominalAttribute) {
            fillDomain(targetAttr as NominalAttribute)
        }

        for (sample in samples) {
            val weight = DoubleArray(targetAttr!!.size)
            val actual = DoubleArray(targetAttr!!.size)
            val target = DoubleArray(targetAttr!!.size)
            if (targetAttr is NominalAttribute) {
                val value = sample.values[targetAttr!!.index]
                val domain = (targetAttr as NominalAttribute).domain
                for (k in 0 until targetAttr!!.size) {
                    actual[k] = if (value == domain!![k]) 1.0 else -1.0
                    target[k] = actual[k]
                }
            } else {
                actual[0] = sample.values[targetAttr!!.index]
                target[0] = actual[0]
            }
            sample.weight = weight
            sample.actual = actual
            sample.target = target
        }

        return DataFrame(
            targetAttr as Attribute,
            features.toTypedArray(),
            samples.toTypedArray(),
            BooleanArray(samples.size, { true }),
            BooleanArray(samples.size, { false })
        )
    }
}
