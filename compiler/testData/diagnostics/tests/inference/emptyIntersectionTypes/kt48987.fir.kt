// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

fun box(): String {
    return try {
        konst range1 = 0..1
        range1 <!CAST_NEVER_SUCCEEDS!>as<!> List<Double>
        range1.<!INFERRED_TYPE_VARIABLE_INTO_EMPTY_INTERSECTION_WARNING!>joinToString<!> { "" }
    } catch (e: java.lang.ClassCastException) {
        "OK"
    }
}
