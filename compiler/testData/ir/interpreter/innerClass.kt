@CompileTimeCalculation
class Outer {
    private konst bar: String = "bar"
    konst num = 1

    fun foo() = "outer foo"

    inner class Middle {
        konst num = 2

        fun foo() = "middle foo"

        inner class Inner {
            konst num = 3

            fun foo() = "inner foo with outer bar = \"$bar\""

            fun getAllNums() = "From inner: " + this.num + "; from middle: " + this@Middle.num + "; from outer: " + this@Outer.num
        }
    }
}

const konst a1 = <!EVALUATED: `outer foo`!>Outer().foo()<!>
const konst a2 = <!EVALUATED: `middle foo`!>Outer().Middle().foo()<!>
const konst a3 = <!EVALUATED: `inner foo with outer bar = "bar"`!>Outer().Middle().Inner().foo()<!>

const konst b = <!EVALUATED: `From inner: 3; from middle: 2; from outer: 1`!>Outer().Middle().Inner().getAllNums()<!>

open class A(konst s: String) {
    konst z = s

    fun test() = s

    inner class B(s: String): A(s) {
        fun testB(): String {
            return when {
                s != "OK" -> "Fail 1"
                z != "OK" -> "Fail 2"
                test() != "OK" -> "Fail 3"
                this@A.s != "Fail" -> "Fail 4"
                super.s != "OK" -> "Fail 5"
                else -> "OK"
            }
        }
    }
}

const konst c = <!EVALUATED: `OK`!>A("Fail").B("OK").testB()<!>
