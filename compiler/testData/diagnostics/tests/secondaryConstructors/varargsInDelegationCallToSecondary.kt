// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER
fun <T> array(vararg x: T): Array<T> = null!!

open class B(x: Int) {
    constructor(vararg y: String): this(y[0].length)
}

class A : B {
    constructor(x: String, y: String): super(x, *array("q"), y)
    constructor(x: String): super(x)
    constructor(): super()
}

konst b1 = B()
konst b2 = B("1", "2", "3")
konst b3 = B("1", *array("2", "3"), "4")
konst b4 = B(1)
