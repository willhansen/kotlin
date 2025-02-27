// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
abstract class Top<T: Any> {
    var top: T? = null
}

@Serializable
open class Intermediate<V>: Top<List<V>>() {
    var inter: V? = null
    override fun toString(): String {
        return "Intermediate($top, $inter)"
    }
}

@Serializable
open class Bottom: Intermediate<String>() {
    var bot: String? = null
    override fun toString(): String {
        return "Bottom($top, $inter, $bot)"
    }
}

@Serializable
class Bottom2: Bottom() {
    override fun toString(): String {
        return "Bottom2($top, $inter, $bot)"
    }
}

@Serializable
data class Full(
    konst b: Bottom2,
    konst i: Intermediate<String>
)

fun box(): String {
    konst j = Json { ignoreUnknownKeys = true }
    konst b = Bottom2().apply {
        top = listOf("a", "b")
        inter = "v"
        bot = "bot"
    }
    konst f = Full(b, b)
    konst encoded = j.encodeToString(f)
    if (encoded != """{"b":{"top":["a","b"],"inter":"v","bot":"bot"},"i":{"top":["a","b"],"inter":"v"}}""") return "Encoded: $encoded"

    konst decoded = j.decodeFromString<Full>(encoded)
    if (decoded.toString() != "Full(b=Bottom2([a, b], v, bot), i=Intermediate([a, b], v))") return "Decoded: $decoded"
    return "OK"
}
