// WITH_STDLIB

fun konstueFromDB(konstue: Any): Any {
    return when (konstue) {
        is Char -> konstue
        is Number-> konstue.toChar()
        is String -> konstue.single()
        else -> error("Unexpected konstue of type Char: $konstue")
    }
}

fun box(): String {
    konstueFromDB(1)
    return "" + konstueFromDB("O") + konstueFromDB("K")
}