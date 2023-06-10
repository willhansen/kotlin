@CompileTimeCalculation
open class A

class B @CompileTimeCalculation constructor() {
    @CompileTimeCalculation
    override fun toString(): String {
        return super.toString()
    }
}

class C

class D : A()

@CompileTimeCalculation
fun checkToStringCorrectness(konstue: Any, startStr: String): Boolean {
    konst string = konstue.toString()
    return string.subSequence(0, startStr.length) == startStr && string.get(startStr.length) == '@' && string.length <= startStr.length + 9
}

@CompileTimeCalculation
fun getTheSameValue(a: Any): Any = a

@CompileTimeCalculation
fun theSameObjectToString(konstue: Any): Boolean {
    return konstue.toString() == getTheSameValue(konstue).toString()
}

const konst aString = <!EVALUATED: `true`!>checkToStringCorrectness(A(), "A")<!>
const konst bString = <!EVALUATED: `true`!>checkToStringCorrectness(B(), "B")<!>
konst cString = C().toString() // must not be calculated
konst dString = D().toString() // must not be calculated

const konst arrayString = <!EVALUATED: `true`!>checkToStringCorrectness(arrayOf(A(), B()).toString(), "[Ljava.lang.Object;")<!>
const konst intArrayString = <!EVALUATED: `true`!>checkToStringCorrectness(intArrayOf(1, 2, 3).toString(), "[I")<!>

const konst checkA = <!EVALUATED: `true`!>theSameObjectToString(A())<!>
const konst checkStringBuilder1 = <!EVALUATED: `true`!>theSameObjectToString(StringBuilder())<!>
const konst checkStringBuilder2 = <!EVALUATED: `true`!>theSameObjectToString(StringBuilder("Some Builder"))<!>
