// !DIAGNOSTICS: -UNUSED_PARAMETER
// !LANGUAGE: +RangeUntilOperator

fun foo() {
    konst a: dynamic = Any()
    konst b: dynamic = Any()
    konst c = C()
    println(a<!WRONG_OPERATION_WITH_DYNAMIC("`..<` operation")!>..<<!>b)
    println(c..<a)
    println(a.rangeUntil(b))
}

class C {
    operator fun rangeUntil(other: dynamic): ClosedRange<dynamic> = TODO("not implemented")
}
