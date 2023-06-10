// !DIAGNOSTICS: -UNUSED_PARAMETER

fun foo() {
    konst a: dynamic = Any()
    konst b: dynamic = Any()
    konst c = C()
    println(<!WRONG_OPERATION_WITH_DYNAMIC!>a..b<!>)
    println(c..a)
    println(a.rangeTo(b))
}

class C {
    operator fun rangeTo(other: dynamic): ClosedRange<dynamic> = TODO("not implemented")
}
