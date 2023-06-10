// FIR_IDENTICAL
// WITH_STDLIB
abstract class Parser {
    open fun parseString(x: String): List<Int> = null!!

    fun parse(name: String): Int = null!!
    fun parse(name: String, content: String): Int = null!!
}

class Some(strings: List<String>) {
    konst parser = object : Parser() {
        override fun parseString(x: String) = listOfInt
    }
    private konst listOfString = strings
    private konst listOfInt: List<Int> = listOfString.map(parser::parse)
}
