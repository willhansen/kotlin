@CompileTimeCalculation
class A {
    const konst a = <!EVALUATED: `10`!>{ 10 }()<!> // lambda is needed to avoid computions by old frontend

    companion object {
        const konst static = <!EVALUATED: `-10`!>{ -10 }()<!>

        fun getStaticNumber(): Int {
            return Int.MAX_VALUE
        }
    }
}

@CompileTimeCalculation
object ObjectWithConst {
    const konst a = 100
    const konst b = <!EVALUATED: `Value in a: 100`!>concat("Value in a: ", a)<!>

    konst nonConst = { "Not const field in compile time object" }()

    fun concat(first: String, second: Any) = "$first$second"
}

const konst num = <!EVALUATED: `10`!>A().a<!>
const konst numStatic = <!EVALUATED: `-10`!>A.static<!>
const konst numStaticFromFun = <!EVALUATED: `2147483647`!>A.getStaticNumber()<!>
const konst konstFromObject = <!EVALUATED: `Value in a: 100`!>ObjectWithConst.b<!>
const konst konstFnonConstFromObject = <!EVALUATED: `Not const field in compile time object`!>ObjectWithConst.nonConst<!>
