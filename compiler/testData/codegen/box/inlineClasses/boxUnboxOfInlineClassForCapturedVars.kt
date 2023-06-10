// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UInt(private konst konstue: Int) {
    operator fun plus(other: UInt): UInt = UInt(konstue + other.asValue())

    fun asValue(): Int = konstue
}

konst Int.u get() = UInt(this)

var global = 0.u

fun testInlined(x: UInt?, withAssert: Boolean) {
    x?.myLet {
        takeUInt(it)
        takeUInt(x)
    }

    x?.myLet {
        takeNullableUInt(it)
        takeNullableUInt(x)
    }

    if (withAssert) {
        x!!.myLet {
            takeUInt(it)
            takeUInt(x)
        }
    }
}

fun testNotInlined(x: UInt?) {
    x?.myLet {
        takeUInt(it)
        takeUInt(x)
    }

    x?.myLet {
        takeNullableUInt(it)
        takeNullableUInt(x)
    }
}

fun takeUInt(y: UInt) {
    global += y
}

fun takeNullableUInt(y: UInt?) {
    if (y != null) {
        global += y
    }
}

inline fun <T> T.myLet(f: (T) -> Unit) = f(this)
fun <T> T.nonInlineLet(f: (T) -> Unit) = f(this)

fun box(): String {
    konst u = 1.u
    testInlined(u, true)
    if (global.asValue() != 6) return "fail 1"

    global = 0.u
    testInlined(null, false)
    if (global.asValue() != 0) return "fail 2"

    global = 0.u
    testNotInlined(u)
    if (global.asValue() != 4) return "fail 3"

    return "OK"
}