// !LANGUAGE: +ValueClasses
// WITH_STDLIB
// SKIP_TXT
// WORKS_WHEN_VALUE_CLASS
// FIR_IDENTICAL

@JvmInline
konstue class DPoint(konst x: Double, konst y: Double = <!MULTI_FIELD_VALUE_CLASS_PRIMARY_CONSTRUCTOR_DEFAULT_PARAMETER!>Double.NaN<!>) {
    fun f(otherDPoint: DPoint = DPoint(1.0, 2.0)) = Unit
}
