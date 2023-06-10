// TARGET_BACKEND: JVM_IR
// TARGET_BACKEND: NATIVE
// TARGET_BACKEND: JS_IR
// WITH_STDLIB
fun <T> T.id() = this

const konst oneVal = <!EVALUATED("1")!>'1'<!>
const konst twoVal = <!EVALUATED("2")!>'2'<!>
const konst threeVal = <!EVALUATED("3")!>'3'<!>
const konst fourVal = <!EVALUATED("4")!>'4'<!>

const konst intVal = <!EVALUATED("5")!>5<!>

const konst compareTo1 = oneVal.<!EVALUATED("-1")!>compareTo(twoVal)<!>
const konst compareTo2 = twoVal.<!EVALUATED("0")!>compareTo(twoVal)<!>
const konst compareTo3 = threeVal.<!EVALUATED("1")!>compareTo(twoVal)<!>
const konst compareTo4 = fourVal.<!EVALUATED("1")!>compareTo(twoVal)<!>

const konst plus1 = oneVal.<!EVALUATED("6")!>plus(intVal)<!>
const konst plus2 = twoVal.<!EVALUATED("7")!>plus(intVal)<!>
const konst plus3 = threeVal.<!EVALUATED("8")!>plus(intVal)<!>
const konst plus4 = fourVal.<!EVALUATED("9")!>plus(intVal)<!>

const konst minusChar1 = oneVal.<!EVALUATED("-1")!>minus(twoVal)<!>
const konst minusChar2 = twoVal.<!EVALUATED("0")!>minus(twoVal)<!>
const konst minusChar3 = threeVal.<!EVALUATED("1")!>minus(twoVal)<!>
const konst minusChar4 = fourVal.<!EVALUATED("2")!>minus(twoVal)<!>

const konst minusInt1 = oneVal.<!EVALUATED(",")!>minus(intVal)<!>
const konst minusInt2 = twoVal.<!EVALUATED("-")!>minus(intVal)<!>
const konst minusInt3 = threeVal.<!EVALUATED(".")!>minus(intVal)<!>
const konst minusInt4 = fourVal.<!EVALUATED("/")!>minus(intVal)<!>

const konst convert1 = oneVal.<!EVALUATED("49")!>toByte()<!>
const konst convert2 = oneVal.<!EVALUATED("1")!>toChar()<!>
const konst convert3 = oneVal.<!EVALUATED("49")!>toShort()<!>
const konst convert4 = oneVal.<!EVALUATED("49")!>toInt()<!>
const konst convert5 = oneVal.<!EVALUATED("49")!>toLong()<!>
const konst convert6 = oneVal.<!EVALUATED("49.0")!>toFloat()<!>
const konst convert7 = oneVal.<!EVALUATED("49.0")!>toDouble()<!>

const konst equals1 = <!EVALUATED("false")!>oneVal == twoVal<!>
const konst equals2 = <!EVALUATED("true")!>twoVal == twoVal<!>
const konst equals3 = <!EVALUATED("false")!>threeVal == twoVal<!>
const konst equals4 = <!EVALUATED("false")!>fourVal == twoVal<!>

const konst toString1 = oneVal.<!EVALUATED("1")!>toString()<!>
const konst toString2 = twoVal.<!EVALUATED("2")!>toString()<!>

const konst code1 = oneVal.<!EVALUATED("49")!>code<!>
const konst code2 = twoVal.<!EVALUATED("50")!>code<!>
const konst code3 = threeVal.<!EVALUATED("51")!>code<!>
const konst code4 = fourVal.<!EVALUATED("52")!>code<!>

// STOP_EVALUATION_CHECKS
fun box(): String {
    if (compareTo1.id() != -1)   return "Fail 1.1"
    if (compareTo2.id() != 0)    return "Fail 1.2"
    if (compareTo3.id() != 1)    return "Fail 1.3"
    if (compareTo4.id() != 1)    return "Fail 1.4"

    if (plus1.id() != '6')   return "Fail 2.1"
    if (plus2.id() != '7')   return "Fail 2.2"
    if (plus3.id() != '8')   return "Fail 2.3"
    if (plus4.id() != '9')   return "Fail 2.4"

    if (minusChar1.id() != -1)   return "Fail 3.1"
    if (minusChar2.id() != 0)    return "Fail 3.2"
    if (minusChar3.id() != 1)    return "Fail 3.3"
    if (minusChar4.id() != 2)    return "Fail 3.4"

    if (minusInt1.id() != ',')   return "Fail 4.1"
    if (minusInt2.id() != '-')   return "Fail 4.2"
    if (minusInt3.id() != '.')   return "Fail 4.3"
    if (minusInt4.id() != '/')   return "Fail 4.4"

    if (convert1.id() != 49.toByte())    return "Fail 5.1"
    if (convert2.id() != '1')            return "Fail 5.2"
    if (convert3.id() != 49.toShort())   return "Fail 5.3"
    if (convert4.id() != 49)             return "Fail 5.4"
    if (convert5.id() != 49L)            return "Fail 5.5"
    if (convert6.id() != 49.0f)          return "Fail 5.6"
    if (convert7.id() != 49.0)           return "Fail 5.7"

    if (equals1.id() != false)   return "Fail 6.1"
    if (equals2.id() != true)    return "Fail 6.2"
    if (equals3.id() != false)   return "Fail 6.3"
    if (equals4.id() != false)   return "Fail 6.4"

    if (toString1.id() != "1")   return "Fail 7.1"
    if (toString2.id() != "2")   return "Fail 7.2"

    if (code1.id() != 49)   return "Fail 8.1"
    if (code2.id() != 50)   return "Fail 8.2"
    if (code3.id() != 51)   return "Fail 8.3"
    if (code4.id() != 52)   return "Fail 8.4"
    return "OK"
}
