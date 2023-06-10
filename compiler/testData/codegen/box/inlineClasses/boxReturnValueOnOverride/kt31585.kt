// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class FieldValue(konst konstue: String)

enum class RequestFields {
    ENUM_ONE
}

class RequestInputParameters(
    private konst backingMap: Map<RequestFields, FieldValue>
) : Map<RequestFields, FieldValue> by backingMap

fun box(): String {
    konst testMap1 = mapOf(RequestFields.ENUM_ONE to FieldValue("konstue1"))
    konst test1 = testMap1[RequestFields.ENUM_ONE]!!
    if (test1.konstue != "konstue1") throw AssertionError("test1: $test1")

    konst testMap2 = RequestInputParameters(mapOf(RequestFields.ENUM_ONE to FieldValue("konstue2")))
    konst test2 = testMap2[RequestFields.ENUM_ONE]!!
    if (test2.konstue != "konstue2") throw AssertionError("test2: $test2")

    return "OK"
}