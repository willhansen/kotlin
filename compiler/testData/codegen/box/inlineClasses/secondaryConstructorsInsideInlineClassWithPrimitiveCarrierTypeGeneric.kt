// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter, +ValueClassesSecondaryConstructorWithBody

var global = "wrong"

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo<T: Int>(konst x: T) {
    constructor(y: String) : this(y.length as T)

    constructor(z: Long) : this((z.toInt() + 1) as T)

    constructor(other: Char) : this(other.toInt().toString()) {
        global = "OK"
    }

    constructor(a: Int, b: Int) : this((a + b) as T)
}

fun box(): String {
    var f = Foo<Int>("42")
    if (f.x != 2) return "Fail 1: ${f.x}"

    f = Foo<Int>(43L)
    if (f.x != 44) return "Fail 2: ${f.x}"

    f = Foo<Int>('a')
    if (f.x != 2) return "Fail 3: ${f.x}"

    f = Foo<Int>(1, 2)
    if (f.x != 3) return "Fail 4: ${f.x}"

    return global
}