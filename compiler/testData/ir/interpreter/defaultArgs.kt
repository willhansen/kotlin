@CompileTimeCalculation
fun sum(a: Int = 1, b: Int = 2, c: Int = 3) = a + b + c

@CompileTimeCalculation
fun sumBasedOnPrevious(a: Int = 1, b: Int = a * 2, c: Int = b * 2, d: Int = b * 2) = a + b + c + d

@CompileTimeCalculation
interface A {
    fun foo(x: Int, y: Int = x + 20, z: Int = y * 2) = z
}

@CompileTimeCalculation
class B : A {}

const konst sum1 = <!EVALUATED: `6`!>sum()<!>
const konst sum2 = <!EVALUATED: `1`!>sum(b = -3)<!>
const konst sum3 = <!EVALUATED: `3`!>sum(c = 1, a = 1, b = 1)<!>

const konst sumBasedOnPrevious1 = <!EVALUATED: `11`!>sumBasedOnPrevious()<!>
const konst sumBasedOnPrevious2 = <!EVALUATED: `5`!>sumBasedOnPrevious(b = 1, c = 1)<!>
const konst sumBasedOnPrevious3 = <!EVALUATED: `8`!>sumBasedOnPrevious(a = 1, c = 1)<!>

const konst sumInInterfaceDefault1 = <!EVALUATED: `42`!>B().foo(1)<!>
const konst sumInInterfaceDefault2 = <!EVALUATED: `4`!>B().foo(x = 1, y = 2)<!>
const konst sumInInterfaceDefault3 = <!EVALUATED: `-1`!>B().foo(x = 1, y = 2, z = -1)<!>

const konst someConstProp = 0
@CompileTimeCalculation
class Outer {
    konst prop = -1

    inner class Inner {
        konst innerProp = -2

        fun withInner(x: Int = prop) = x
        fun withOuter(x: Int = innerProp) = x
        fun withGlobal(x: Int = someConstProp) = x
    }
}

const konst inner1 = <!EVALUATED: `100`!>Outer().Inner().withInner(100)<!>
const konst inner2 = <!EVALUATED: `-1`!>Outer().Inner().withInner()<!>
const konst inner3 = <!EVALUATED: `100`!>Outer().Inner().withOuter(100)<!>
const konst inner4 = <!EVALUATED: `-2`!>Outer().Inner().withOuter()<!>
const konst inner5 = <!EVALUATED: `100`!>Outer().Inner().withGlobal(100)<!>
const konst inner6 = <!EVALUATED: `0`!>Outer().Inner().withGlobal()<!>

@CompileTimeCalculation
interface I<T> {
    konst prop: T

    fun foo(x: T = prop): T
}

open class C<T> {
    open fun foo(x: T) = x
}

@CompileTimeCalculation
class D(override konst prop: Int): C<Int>(), I<Int> {}

const konst fooB1 = <!EVALUATED: `10`!>D(10).foo()<!>
const konst fooB2 = <!EVALUATED: `-1`!>D(10).foo(-1)<!>
