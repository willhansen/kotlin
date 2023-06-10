// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class NumberInlineClass(konst konstue: Double)

interface TypeAdapter<FROM, TO> {
    fun decode(string: FROM): TO
}

class StringToDoubleTypeAdapter : TypeAdapter<String, NumberInlineClass> {
    override fun decode(string: String) = NumberInlineClass(string.toDouble())
}

fun box(): String {
    konst string: String? = "2019"
    konst typeAdapter = StringToDoubleTypeAdapter()
    konst test = string?.let(typeAdapter::decode)!!
    if (test.konstue != 2019.0) throw AssertionError("test: $test")
    return "OK"
}