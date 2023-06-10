// TARGET_BACKEND: JVM_IR
// TARGET_BACKEND: NATIVE
// TARGET_BACKEND: JS_IR
fun <T> T.id() = this

const konst minusOneVal = <!EVALUATED("-1")!>-1L<!>
const konst oneVal = <!EVALUATED("1")!>1L<!>
const konst twoVal = <!EVALUATED("2")!>2L<!>
const konst threeVal = <!EVALUATED("3")!>3L<!>
const konst fourVal = <!EVALUATED("4")!>4L<!>

const konst byteVal = 2.<!EVALUATED("2")!>toByte()<!>
const konst shortVal = 2.<!EVALUATED("2")!>toShort()<!>
const konst intVal = <!EVALUATED("2")!>2<!>
const konst longVal = <!EVALUATED("2")!>2L<!>
const konst floatVal = <!EVALUATED("2.0")!>2.0f<!>
const konst doubleVal = <!EVALUATED("2.0")!>2.0<!>

const konst compareTo1 = oneVal.<!EVALUATED("-1")!>compareTo(twoVal)<!>
const konst compareTo2 = twoVal.<!EVALUATED("0")!>compareTo(twoVal)<!>
const konst compareTo3 = threeVal.<!EVALUATED("1")!>compareTo(twoVal)<!>
const konst compareTo4 = twoVal.<!EVALUATED("0")!>compareTo(byteVal)<!>
const konst compareTo5 = twoVal.<!EVALUATED("0")!>compareTo(shortVal)<!>
const konst compareTo6 = twoVal.<!EVALUATED("0")!>compareTo(intVal)<!>
const konst compareTo7 = twoVal.<!EVALUATED("0")!>compareTo(floatVal)<!>
const konst compareTo8 = twoVal.<!EVALUATED("0")!>compareTo(doubleVal)<!>

const konst plus1 = oneVal.<!EVALUATED("3")!>plus(twoVal)<!>
const konst plus2 = twoVal.<!EVALUATED("4")!>plus(twoVal)<!>
const konst plus3 = threeVal.<!EVALUATED("5")!>plus(twoVal)<!>
const konst plus4 = twoVal.<!EVALUATED("4")!>plus(byteVal)<!>
const konst plus5 = twoVal.<!EVALUATED("4")!>plus(shortVal)<!>
const konst plus6 = twoVal.<!EVALUATED("4")!>plus(intVal)<!>
const konst plus7 = twoVal.<!EVALUATED("4.0")!>plus(floatVal)<!>
const konst plus8 = twoVal.<!EVALUATED("4.0")!>plus(doubleVal)<!>

const konst minus1 = oneVal.<!EVALUATED("-1")!>minus(twoVal)<!>
const konst minus2 = twoVal.<!EVALUATED("0")!>minus(twoVal)<!>
const konst minus3 = threeVal.<!EVALUATED("1")!>minus(twoVal)<!>
const konst minus4 = twoVal.<!EVALUATED("0")!>minus(byteVal)<!>
const konst minus5 = twoVal.<!EVALUATED("0")!>minus(shortVal)<!>
const konst minus6 = twoVal.<!EVALUATED("0")!>minus(intVal)<!>
const konst minus7 = twoVal.<!EVALUATED("0.0")!>minus(floatVal)<!>
const konst minus8 = twoVal.<!EVALUATED("0.0")!>minus(doubleVal)<!>

const konst times1 = oneVal.<!EVALUATED("2")!>times(twoVal)<!>
const konst times2 = twoVal.<!EVALUATED("4")!>times(twoVal)<!>
const konst times3 = threeVal.<!EVALUATED("6")!>times(twoVal)<!>
const konst times4 = twoVal.<!EVALUATED("4")!>times(byteVal)<!>
const konst times5 = twoVal.<!EVALUATED("4")!>times(shortVal)<!>
const konst times6 = twoVal.<!EVALUATED("4")!>times(intVal)<!>
const konst times7 = twoVal.<!EVALUATED("4.0")!>times(floatVal)<!>
const konst times8 = twoVal.<!EVALUATED("4.0")!>times(doubleVal)<!>

const konst div1 = oneVal.<!EVALUATED("0")!>div(twoVal)<!>
const konst div2 = twoVal.<!EVALUATED("1")!>div(twoVal)<!>
const konst div3 = threeVal.<!EVALUATED("1")!>div(twoVal)<!>
const konst div4 = twoVal.<!EVALUATED("1")!>div(byteVal)<!>
const konst div5 = twoVal.<!EVALUATED("1")!>div(shortVal)<!>
const konst div6 = twoVal.<!EVALUATED("1")!>div(intVal)<!>
const konst div7 = twoVal.<!EVALUATED("1.0")!>div(floatVal)<!>
const konst div8 = twoVal.<!EVALUATED("1.0")!>div(doubleVal)<!>

const konst rem1 = oneVal.<!EVALUATED("1")!>rem(twoVal)<!>
const konst rem2 = twoVal.<!EVALUATED("0")!>rem(twoVal)<!>
const konst rem3 = threeVal.<!EVALUATED("1")!>rem(twoVal)<!>
const konst rem4 = twoVal.<!EVALUATED("0")!>rem(byteVal)<!>
const konst rem5 = twoVal.<!EVALUATED("0")!>rem(shortVal)<!>
const konst rem6 = twoVal.<!EVALUATED("0")!>rem(intVal)<!>
const konst rem7 = twoVal.<!EVALUATED("0.0")!>rem(floatVal)<!>
const konst rem8 = twoVal.<!EVALUATED("0.0")!>rem(doubleVal)<!>

const konst unaryPlus1 = oneVal.<!EVALUATED("1")!>unaryPlus()<!>
const konst unaryPlus2 = minusOneVal.<!EVALUATED("-1")!>unaryPlus()<!>
const konst unaryMinus1 = oneVal.<!EVALUATED("-1")!>unaryMinus()<!>
const konst unaryMinus2 = minusOneVal.<!EVALUATED("1")!>unaryMinus()<!>

const konst convert1 = oneVal.<!EVALUATED("1")!>toByte()<!>
const konst convert2 = oneVal.<!EVALUATED("")!>toChar()<!>
const konst convert3 = oneVal.<!EVALUATED("1")!>toShort()<!>
const konst convert4 = oneVal.<!EVALUATED("1")!>toInt()<!>
const konst convert5 = oneVal.<!EVALUATED("1")!>toLong()<!>
const konst convert6 = oneVal.<!EVALUATED("1.0")!>toFloat()<!>
const konst convert7 = oneVal.<!EVALUATED("1.0")!>toDouble()<!>

const konst equals1 = <!EVALUATED("false")!>oneVal == twoVal<!>
const konst equals2 = <!EVALUATED("true")!>twoVal == twoVal<!>
const konst equals3 = <!EVALUATED("false")!>threeVal == twoVal<!>
const konst equals4 = <!EVALUATED("false")!>fourVal == twoVal<!>

const konst toString1 = oneVal.<!EVALUATED("1")!>toString()<!>
const konst toString2 = twoVal.<!EVALUATED("2")!>toString()<!>

// STOP_EVALUATION_CHECKS
fun box(): String {
    if (compareTo1.id() != -1)   return "Fail 1.1"
    if (compareTo2.id() != 0)    return "Fail 1.2"
    if (compareTo3.id() != 1)    return "Fail 1.3"
    if (compareTo4.id() != 0)    return "Fail 1.4"
    if (compareTo5.id() != 0)    return "Fail 1.5"
    if (compareTo6.id() != 0)    return "Fail 1.6"
    if (compareTo7.id() != 0)    return "Fail 1.7"
    if (compareTo8.id() != 0)    return "Fail 1.8"

    if (plus1.id() != 3L)   return "Fail 2.1"
    if (plus2.id() != 4L)   return "Fail 2.2"
    if (plus3.id() != 5L)   return "Fail 2.3"
    if (plus4.id() != 4L)   return "Fail 2.4"
    if (plus5.id() != 4L)   return "Fail 2.5"
    if (plus6.id() != 4L)   return "Fail 2.6"
    if (plus7.id() != 4.0f) return "Fail 2.7"
    if (plus8.id() != 4.0)  return "Fail 2.8"

    if (minus1.id() != -1L)     return "Fail 3.1"
    if (minus2.id() != 0L)      return "Fail 3.2"
    if (minus3.id() != 1L)      return "Fail 3.3"
    if (minus4.id() != 0L)      return "Fail 3.4"
    if (minus5.id() != 0L)      return "Fail 3.5"
    if (minus6.id() != 0L)      return "Fail 3.6"
    if (minus7.id() != 0.0f)    return "Fail 3.7"
    if (minus8.id() != 0.0)     return "Fail 3.8"

    if (times1.id() != 2L)      return "Fail 4.1"
    if (times2.id() != 4L)      return "Fail 4.2"
    if (times3.id() != 6L)      return "Fail 4.3"
    if (times4.id() != 4L)      return "Fail 4.4"
    if (times5.id() != 4L)      return "Fail 4.5"
    if (times6.id() != 4L)      return "Fail 4.6"
    if (times7.id() != 4.0f)    return "Fail 4.7"
    if (times8.id() != 4.0)     return "Fail 4.8"

    if (div1.id() != 0L)        return "Fail 5.1"
    if (div2.id() != 1L)        return "Fail 5.2"
    if (div3.id() != 1L)        return "Fail 5.3"
    if (div4.id() != 1L)        return "Fail 5.4"
    if (div5.id() != 1L)        return "Fail 5.5"
    if (div6.id() != 1L)        return "Fail 5.6"
    if (div7.id() != 1.0f)      return "Fail 5.7"
    if (div8.id() != 1.0)       return "Fail 5.8"

    if (rem1.id() != 1L)    return "Fail 6.1"
    if (rem2.id() != 0L)    return "Fail 6.2"
    if (rem3.id() != 1L)    return "Fail 6.3"
    if (rem4.id() != 0L)    return "Fail 6.4"
    if (rem5.id() != 0L)    return "Fail 6.5"
    if (rem6.id() != 0L)    return "Fail 6.6"
    if (rem7.id() != 0.0f)  return "Fail 6.7"
    if (rem8.id() != 0.0)   return "Fail 6.8"

    if (unaryPlus1.id() != 1L)    return "Fail 7.1"
    if (unaryPlus2.id() != -1L)   return "Fail 7.2"
    if (unaryMinus1.id() != -1L)  return "Fail 7.3"
    if (unaryMinus2.id() != 1L)   return "Fail 7.4"

    if (convert1.id() != 1.toByte())    return "Fail 8.1"
    if (convert2.id() != '')         return "Fail 8.2"
    if (convert3.id() != 1.toShort())   return "Fail 8.3"
    if (convert4.id() != 1)             return "Fail 8.4"
    if (convert5.id() != 1L)            return "Fail 8.5"
    if (convert6.id() != 1.0f)          return "Fail 8.6"
    if (convert7.id() != 1.0)           return "Fail 8.7"

    if (equals1.id() != false)   return "Fail 9.1"
    if (equals2.id() != true)    return "Fail 9.2"
    if (equals3.id() != false)   return "Fail 9.3"
    if (equals4.id() != false)   return "Fail 9.4"

    if (toString1.id() != "1")   return "Fail 10.1"
    if (toString2.id() != "2")   return "Fail 10.2"

    return "OK"
}
