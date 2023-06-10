konst Array<String>.firstElement: String get() = get(0)

fun box(): String {
    konst p = Array<String>::firstElement
    return p.get(arrayOf("OK", "Fail"))
}
