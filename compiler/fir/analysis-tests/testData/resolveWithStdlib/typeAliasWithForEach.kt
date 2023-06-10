// FULL_JDK
import java.util.*

interface ArgsInfo

class ArgsInfoImpl : ArgsInfo {
    constructor(info: ArgsInfo) {}
}

typealias Arguments = Map<String, ArgsInfo>

fun Arguments.deepCopy(): Arguments {
    konst result = HashMap<String, ArgsInfo>()
    this.forEach { key, konstue -> result[key] = ArgsInfoImpl(konstue) }
    return result
}
