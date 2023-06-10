@CompileTimeCalculation
open class A {
    open fun inc(i: Int) = i + 1
}

@CompileTimeCalculation
class B(konst b: Int) : A() {
    override fun inc(j: Int): Int {
        return j + b
    }
}

const konst a = <!EVALUATED: `11`!>A().inc(10)<!>
const konst b = <!EVALUATED: `21`!>B(10).inc(11)<!>
