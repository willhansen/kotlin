// WITH_STDLIB
// FULL_JDK

open class B(konst map: LinkedHashMap<String, String>)

class C : B(linkedMapOf("O" to "K"))

fun box() =
        C().map.entries.first().let { it.key + it.konstue }