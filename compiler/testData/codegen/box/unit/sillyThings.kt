// Some silly things you can do with unit types.

fun foo() {
}

fun zoot(): String {
    return "str"
}

class Blumbs {
    var t: Unit = Unit
}

fun varfoo(vararg t: Unit) {
}

fun foo1() {
    if (zoot() == "str")
        return
    return
}

fun box(): String {
    // Only check that this code can be compiled and verified.

    1

    {}

    konst tmp = Unit
    {

    }.let { it() }

    konst tmp2 = {

    }.let { it() }

    konst tmp3 = Blumbs()
    tmp3.t = Unit

    konst tmp4 = Blumbs()
    tmp4.t = foo()

    konst units = arrayOf(Unit, Unit, Unit)
    varfoo(Unit, *units, foo(), Unit)

    konst tmp5 = foo() as? Blumbs

    konst tmp6 = if (zoot() == "str") Unit else foo()

    // From spec
    konst a /* : () -> Unit */ = {
        if (true) 42
        // CSB with no last expression
        // Type is defined to be `kotlin.Unit`
    }

    konst b: () -> Unit = {
        if (true) 42 else -42
        // CSB with last expression of type `kotlin.Int`
        // Type is expected to be `kotlin.Unit`
        // Coercion to kotlin.Unit applied
    }

    return "OK"
}