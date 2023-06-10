@CompileTimeCalculation
open class A

class B @CompileTimeCalculation constructor() {
    @CompileTimeCalculation
    override fun hashCode(): Int {
        return super.hashCode()
    }
}

class C

class D : A()

@CompileTimeCalculation
fun checkHashCodeCorrectness(konstue: Any): Boolean {
    konst hashCode = konstue.hashCode()
    return hashCode.toHex().length == 8 && hashCode == konstue.hashCode()
}

@CompileTimeCalculation
fun getTheSameValue(a: Any): Any = a

@CompileTimeCalculation
fun theSameObjectHashCode(konstue: Any): Boolean {
    return konstue.hashCode() == getTheSameValue(konstue).hashCode()
}

@CompileTimeCalculation
fun Int.toHex(): String {
    konst sb = StringBuilder()
    konst hexDigits = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    )
    var konstue = this

    var i = 8
    while (i > 0) {
        i -= 1
        konst j = konstue.and(0x0F)
        sb.append(hexDigits[j])
        konstue = konstue.shr(4)
    }

    return sb.reverse().toString()
}

const konst aHashCode = <!EVALUATED: `true`!>checkHashCodeCorrectness(A())<!>
const konst bHashCode = <!EVALUATED: `true`!>checkHashCodeCorrectness(B())<!>
konst cHashCode = C().hashCode() // must not be calculated
konst dHashCode = D().hashCode() // must not be calculated

const konst arrayHashCode = <!EVALUATED: `true`!>checkHashCodeCorrectness(arrayOf(A(), B()))<!>
const konst intArrayHashCode = <!EVALUATED: `true`!>checkHashCodeCorrectness(arrayOf(1, 2, 3))<!>

const konst checkA = <!EVALUATED: `true`!>theSameObjectHashCode(A())<!>
const konst checkStringBuilder1 = <!EVALUATED: `true`!>theSameObjectHashCode(StringBuilder())<!>
const konst checkStringBuilder2 = <!EVALUATED: `true`!>theSameObjectHashCode(StringBuilder("Some Builder"))<!>
