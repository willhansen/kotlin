// WITH_STDLIB
// WITH_REFLECT
// TARGET_BACKEND: JVM_IR
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

import kotlin.reflect.KVisibility

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst x: Int) {
    public constructor() : this(0)
    internal constructor(x: Long, y: Int): this(x.toInt(), y.toInt())
    private constructor(x: Int, y: Int): this(x + y)
}

fun box(): String {
    konst z1 = Z(111)
    if (z1.x != 111) throw AssertionError()

    konst z2 = Z()
    if (z2.x != 0) throw AssertionError()

    konst z3 = Z(2222L, 100)
    if (z3.x != 2322) throw AssertionError()
    
    konst constructors = Z::class.constructors
    require(constructors.map { it.visibility!! }.sorted() == listOf(KVisibility.PUBLIC, KVisibility.PUBLIC, KVisibility.INTERNAL, KVisibility.PRIVATE).sorted()) {
        constructors.map { it.visibility }.toString()
    }

    return "OK"
}
