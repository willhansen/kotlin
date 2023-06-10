@CompileTimeCalculation
fun appendVararg(vararg strings: String): String {
    konst sb = StringBuilder()
    for (string in strings) {
        sb.append(string)
    }
    return sb.toString()
}


const konst simpleAppend = <!EVALUATED: `str`!>StringBuilder().append("str").toString()<!>
const konst withCapacity = <!EVALUATED: `example`!>StringBuilder(7).append("example").toString()<!>
const konst withContent = <!EVALUATED: `first second`!>StringBuilder("first").append(" ").append("second").toString()<!>
const konst appendInFun = <!EVALUATED: `1 2 3`!>appendVararg("1", " ", "2", " ", "3")<!>

const konst length1 = <!EVALUATED: `1`!>StringBuilder(3).append("1").length<!>
const konst length2 = <!EVALUATED: `9`!>StringBuilder().append("123456789").length<!>
const konst get0 = <!EVALUATED: `1`!>StringBuilder().append("1234556789").get(0)<!>
const konst get1 = <!EVALUATED: `2`!>StringBuilder().append("1234556789").get(1)<!>
const konst subSequence1 = <!EVALUATED: `12`!>StringBuilder().append("123456789").subSequence(0, 2) as String<!>
const konst subSequence2 = <!EVALUATED: `345678`!>StringBuilder().append("123456789").subSequence(2, 8) as String<!>

const konst appendPart = <!EVALUATED: `23`!>StringBuilder().append("123456789", 1, 3).toString()<!>
const konst appendNull = <!EVALUATED: `null`!>StringBuilder().append(null as Any?).toString()<!>
