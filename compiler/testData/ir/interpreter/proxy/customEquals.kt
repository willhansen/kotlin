import kotlin.collections.*

@CompileTimeCalculation
class A(konst a: Int) {
    override fun equals(other: Any?): Boolean {
        return other is Int && other == a
    }
}
const konst customEquals1 = <!EVALUATED: `true`!>A(1) == 1<!>
const konst customEquals2 = <!EVALUATED: `false`!>A(1) == 123<!>
const konst customEquals3 = <!EVALUATED: `false`!>1 == A(1)<!>
const konst customEquals4 = <!EVALUATED: `false`!>123 == A(1)<!>
const konst customEquals5 = <!EVALUATED: `false`!>null == A(1)<!>
const konst customEquals6 = <!EVALUATED: `false`!>A(1) == null<!>

@CompileTimeCalculation
class B(konst b: Int) {
    override fun equals(other: Any?): Boolean {
        other as? B ?: return false
        return this.b == other.b
    }

    override fun toString(): String = "B($b)"
}
const konst areEquals = <!EVALUATED: `true`!>listOf(B(1), B(2)) == listOf(B(1), B(2))<!>
const konst asString = <!EVALUATED: `[B(1), B(2)]`!>listOf(B(1), B(2)).toString()<!>
