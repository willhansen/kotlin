// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

class A<F> {
    fun <E : F> foo1(x: E) = x
    fun <E : F?> foo2(x: E) = x

    fun <Z : F, W : Z?> bar(x: F, y: F?, z: Z, w: W) {
        foo1<F>(x)

        konst x1 = foo1(x)
        x1.checkType { _<F>() }

        foo2<F>(x)

        konst x2 = foo2(x)
        x2.checkType { _<F>() }

        foo1<<!UPPER_BOUND_VIOLATED!>F?<!>>(<!ARGUMENT_TYPE_MISMATCH!>y<!>)
        foo1(<!ARGUMENT_TYPE_MISMATCH!>y<!>)
        foo2<F?>(y)

        konst x3 = foo2(y)
        x3.checkType { _<F?>() }

        foo1<F>(<!ARGUMENT_TYPE_MISMATCH!>y<!>)
        foo2<F>(<!ARGUMENT_TYPE_MISMATCH!>y<!>)

        foo1<Z>(z)

        konst x4 = foo1(z)
        x4.checkType { _<Z>() }

        foo2<Z>(z)

        konst x5 = foo2(z)
        x4.checkType { _<Z>() }

        foo1<<!UPPER_BOUND_VIOLATED!>W<!>>(<!ARGUMENT_TYPE_MISMATCH!>w<!>)
        foo1(<!ARGUMENT_TYPE_MISMATCH!>w<!>)
        foo2<W>(w)

        konst x6 = foo2(w)
        x6.checkType { _<W>() }

        foo1<<!UPPER_BOUND_VIOLATED!>W<!>>(<!ARGUMENT_TYPE_MISMATCH!>w<!>)
    }
}
