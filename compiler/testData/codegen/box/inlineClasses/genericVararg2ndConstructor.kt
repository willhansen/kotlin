// WITH_STDLIB
// KT-41771
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Polynomial<T : Any>(konst coefficients: List<T>) {
    constructor(vararg coefficients: T) : this(coefficients.toList())
}
fun box(): String {
    konst p = Polynomial("FAIL1", "OK", "FAIL2")
    return p.coefficients[1]
}