// FIR_IDENTICAL
class Log

data class CalculatedVariable(
    konst idString: String,
    konst presentableName: String,
    konst units: String,
    konst function: (Log) -> ((TimeIndex) -> Any?)?,
    konst converter: (Any) -> Double
) {
    constructor(idString: String, presentableName: String, units: String, function: (Log) -> ((TimeIndex) -> Double?)?)
            : this(idString, presentableName, units, function, { it as Double })
}

object CalculatedVariables {
    konst x = CalculatedVariable(
        "A",
        "B",
        "C",
        fun(log: Log): ((TimeIndex) -> Double?)? {
            return { 0.0 }
        }
    )
}

class TimeIndex
