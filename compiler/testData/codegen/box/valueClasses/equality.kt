// WITH_STDLIB
// TARGET_BACKEND: JVM_IR
// LANGUAGE: +ValueClasses
// CHECK_BYTECODE_LISTING

@JvmInline
konstue class F1(konst x: Int)

@JvmInline
konstue class F2(konst x: UInt)

@JvmInline
konstue class F3(konst x: F1, konst y: F2)

@JvmInline
konstue class F4(konst x: Int)

@JvmInline
konstue class F5(konst x: UInt)

@JvmInline
konstue class F6(konst x: String)

@JvmInline
konstue class A(
    konst f1: F1,
    konst f2: F2,
    konst f3: F3,
    konst f4: F4,
    konst f5: F5,
    konst f6: F6,
    konst f7: Int,
    konst f8: UInt,
    konst f9: String,
)

@JvmInline
konstue class B(konst a1: A, konst a2: A) {
    override fun toString(): String {
        return "OverridenBToString(a1 = $a1, a2 = $a2)"
    }
}

fun box(): String {
    konst f1 = F1(1)
    konst f2 = F2(UInt.MAX_VALUE)
    konst f3 = F3(f1, f2)
    konst f4 = F4(5)
    konst f5 = F5(UInt.MAX_VALUE.dec())
    konst f6 = F6("678")
    konst a1 = A(f1, f2, f3, f4, f5, f6, 9, UInt.MAX_VALUE - 2U, "0")
    konst a2 = A(f1, f2, f3, f4, f5, f6, 9, UInt.MAX_VALUE - 2U, "0")
    konst b = B(a1, a2)

    assert(f1.x == 1)
    assert(f2.x == UInt.MAX_VALUE)
    assert(f3.x == f1)
    assert(f3.x.x == 1)
    assert(f3.y == f2)
    assert(f3.y.x == UInt.MAX_VALUE)
    assert(f4.x == 5)
    assert(f5.x == UInt.MAX_VALUE - 1U)
    assert(f6.x == "678")

    assert(f1 == a1.f1)
    assert(f1.hashCode() == a1.f1.hashCode())
    assert(f1.toString() == a1.f1.toString())
    assert(f1 == a2.f1)
    assert(f1.hashCode() == a2.f1.hashCode())
    assert(f1.toString() == a2.f1.toString())
    assert(a1.f1 == a2.f1)
    assert(a1.f1.hashCode() == a2.f1.hashCode())
    assert(a1.f1.toString() == a2.f1.toString())

    assert(f2 == a1.f2)
    assert(f2.hashCode() == a1.f2.hashCode())
    assert(f2.toString() == a1.f2.toString())
    assert(f2 == a2.f2)
    assert(f2.hashCode() == a2.f2.hashCode())
    assert(f2.toString() == a2.f2.toString())
    assert(a1.f2 == a2.f2)
    assert(a1.f2.hashCode() == a2.f2.hashCode())
    assert(a1.f2.toString() == a2.f2.toString())

    assert(f1 == a1.f3.x)
    assert(f1.hashCode() == a1.f3.x.hashCode())
    assert(f1.toString() == a1.f3.x.toString())
    assert(f1 == a2.f3.x)
    assert(f1.hashCode() == a2.f3.x.hashCode())
    assert(f1.toString() == a2.f3.x.toString())
    assert(a1.f3.x == a2.f3.x)
    assert(a1.f3.x.hashCode() == a2.f3.x.hashCode())
    assert(a1.f3.x.toString() == a2.f3.x.toString())

    assert(f2 == a1.f3.y)
    assert(f2.hashCode() == a1.f3.y.hashCode())
    assert(f2.toString() == a1.f3.y.toString())
    assert(f2 == a2.f3.y)
    assert(f2.hashCode() == a2.f3.y.hashCode())
    assert(f2.toString() == a2.f3.y.toString())
    assert(a1.f3.y == a2.f3.y)
    assert(a1.f3.y.hashCode() == a2.f3.y.hashCode())
    assert(a1.f3.y.toString() == a2.f3.y.toString())

    assert(f3 == a1.f3)
    assert(f3.hashCode() == a1.f3.hashCode())
    assert(f3.toString() == a1.f3.toString())
    assert(f3 == a2.f3)
    assert(f3.hashCode() == a2.f3.hashCode())
    assert(f3.toString() == a2.f3.toString())
    assert(a1.f3 == a2.f3)
    assert(a1.f3.hashCode() == a2.f3.hashCode())
    assert(a1.f3.toString() == a2.f3.toString())

    assert(f4 == a1.f4)
    assert(f4.hashCode() == a1.f4.hashCode())
    assert(f4.toString() == a1.f4.toString())
    assert(f4 == a2.f4)
    assert(f4.hashCode() == a2.f4.hashCode())
    assert(f4.toString() == a2.f4.toString())
    assert(a1.f4 == a2.f4)
    assert(a1.f4.hashCode() == a2.f4.hashCode())
    assert(a1.f4.toString() == a2.f4.toString())

    assert(f5 == a1.f5)
    assert(f5.hashCode() == a1.f5.hashCode())
    assert(f5.toString() == a1.f5.toString())
    assert(f5 == a2.f5)
    assert(f5.hashCode() == a2.f5.hashCode())
    assert(f5.toString() == a2.f5.toString())
    assert(a1.f5 == a2.f5)
    assert(a1.f5.hashCode() == a2.f5.hashCode())
    assert(a1.f5.toString() == a2.f5.toString())

    assert(f6 == a1.f6)
    assert(f6.hashCode() == a1.f6.hashCode())
    assert(f6.toString() == a1.f6.toString())
    assert(f6 == a2.f6)
    assert(f6.hashCode() == a2.f6.hashCode())
    assert(f6.toString() == a2.f6.toString())
    assert(a1.f6 == a2.f6)
    assert(a1.f6.hashCode() == a2.f6.hashCode())
    assert(a1.f6.toString() == a2.f6.toString())

    assert(9 == a1.f7)
    assert(9.hashCode() == a1.f7.hashCode())
    assert(9.toString() == a1.f7.toString())
    assert(9 == a2.f7)
    assert(9.hashCode() == a2.f7.hashCode())
    assert(9.toString() == a2.f7.toString())
    assert(a1.f7 == a2.f7)
    assert(a1.f7.hashCode() == a2.f7.hashCode())
    assert(a1.f7.toString() == a2.f7.toString())

    assert((UInt.MAX_VALUE - 2U) == a1.f8)
    assert((UInt.MAX_VALUE - 2U).hashCode() == a1.f8.hashCode())
    assert((UInt.MAX_VALUE - 2U).toString() == a1.f8.toString())
    assert((UInt.MAX_VALUE - 2U) == a2.f8)
    assert((UInt.MAX_VALUE - 2U).hashCode() == a2.f8.hashCode())
    assert((UInt.MAX_VALUE - 2U).toString() == a2.f8.toString())
    assert(a1.f8 == a2.f8)
    assert(a1.f8.hashCode() == a2.f8.hashCode())
    assert(a1.f8.toString() == a2.f8.toString())

    assert("0" == a1.f9)
    assert("0".hashCode() == a1.f9.hashCode())
    assert("0".toString() == a1.f9.toString())
    assert("0" == a2.f9)
    assert("0".hashCode() == a2.f9.hashCode())
    assert("0".toString() == a2.f9.toString())
    assert(a1.f9 == a2.f9)
    assert(a1.f9.hashCode() == a2.f9.hashCode())
    assert(a1.f9.toString() == a2.f9.toString())


    assert(a1 == a2)
    assert(a1.hashCode() == a2.hashCode())
    assert(a1.toString() == a2.toString())

    assert(b == b)
    assert(b.toString() == b.toString())
    assert(b.hashCode() == b.hashCode())


    assert(f1.toString() == "F1(x=1)") { f1.toString() }
    assert(f2.toString() == "F2(x=4294967295)") { f2.toString() }
    assert(f3.toString() == "F3(x=F1(x=1), y=F2(x=4294967295))") { f3.toString() }
    assert(f4.toString() == "F4(x=5)") { f4.toString() }
    assert(f5.toString() == "F5(x=4294967294)") { f5.toString() }
    assert(f6.toString() == "F6(x=678)") { f6.toString() }
    konst aStr = "A(f1=F1(x=1), f2=F2(x=4294967295), f3=F3(x=F1(x=1), y=F2(x=4294967295)), f4=F4(x=5), f5=F5(x=4294967294), f6=F6(x=678), f7=9, f8=4294967293, f9=0)"
    assert(a1.toString() == aStr) { a1.toString() }
    assert(a2.toString() == aStr) { a2.toString() }
    assert(b.toString() == "OverridenBToString(a1 = $aStr, a2 = $aStr)") { b.toString() }

    return "OK"
}
