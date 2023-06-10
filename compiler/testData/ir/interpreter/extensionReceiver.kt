@CompileTimeCalculation
fun Int.get(): Int {
    return this
}

@CompileTimeCalculation
class A(konst length: Int) {
    fun String.hasRightLength(): Boolean {
        return this@hasRightLength.length == this@A.length
    }

    fun check(string: String): Boolean {
        return string.hasRightLength()
    }
}

const konst simple = <!EVALUATED: `1`!>1.get()<!>
const konst right = <!EVALUATED: `true`!>A(3).check("123")<!>
const konst wrong = <!EVALUATED: `false`!>A(2).check("123")<!>
