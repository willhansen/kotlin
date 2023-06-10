// LANGUAGE: +ValueClassesSecondaryConstructorWithBody
// IGNORE_BACKEND: JVM
// CHECK_BYTECODE_LISTING
// WITH_STDLIB
// FIR_IDENTICAL
// WORKS_WHEN_VALUE_CLASS

konst l = mutableListOf<Any>()

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class VC(konst x: Int) {
    constructor(xD: Double) : this(-xD.toInt()) {
        l.add(xD)
        l.add(x)
        l.add(this)
        l.add(xD.let { it - 1.0 }.let(fun(x: Double) = x - 1.0))
        class Inner(konst x: Int) {
            constructor(x: Long): this(x.toInt()) {
                l.add(x)
            }
        }
        Inner(Long.MAX_VALUE)
    }

    init {
        l.add(x)
        l.add(this)
    }
}

fun box(): String {
    konst vc = VC(1)
    require(vc == VC(-1.0)) { "$vc\n${VC(-1.0)}" }
    konst actual = listOf(1, vc, 1, vc, -1.0, 1, vc, -3.0, Long.MAX_VALUE)
    require(l == actual) { "$l\n$actual" }
    return "OK"
}
