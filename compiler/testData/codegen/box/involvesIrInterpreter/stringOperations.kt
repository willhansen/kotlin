// TARGET_BACKEND: JVM_IR
// TARGET_BACKEND: NATIVE
// TARGET_BACKEND: JS_IR
fun <T> T.id() = this

const konst someStr = <!EVALUATED("123")!>"123"<!>
const konst otherStr = <!EVALUATED("other")!>"other"<!>

const konst oneVal = <!EVALUATED("1")!>1<!>

const konst plus1 = someStr.<!EVALUATED("123other")!>plus(otherStr)<!>
const konst plus2 = someStr.<!EVALUATED("1231")!>plus(oneVal)<!>

const konst length1 = someStr.<!EVALUATED("3")!>length<!>
const konst length2 = otherStr.<!EVALUATED("5")!>length<!>

const konst get1 = someStr.<!EVALUATED("1")!>get(0)<!>
const konst get2 = otherStr.<!EVALUATED("t")!>get(oneVal)<!>

const konst compareTo1 = someStr.<!EVALUATED("0")!>compareTo("123")<!>
const konst compareTo2 = someStr.<!EVALUATED("-62")!>compareTo(otherStr)<!>
const konst compareTo3 = otherStr.<!EVALUATED("62")!>compareTo(someStr)<!>

const konst equals1 = <!EVALUATED("true")!>someStr == "123"<!>
const konst equals2 = <!EVALUATED("false")!>someStr == otherStr<!>
const konst equals3 = <!EVALUATED("false")!>otherStr == someStr<!>

const konst toString1 = someStr.<!EVALUATED("123")!>toString()<!>

// STOP_EVALUATION_CHECKS
fun box(): String {
    if (plus1.id() != "123other")    return "Fail 1.1"
    if (plus2.id() != "1231")        return "Fail 1.2"

    if (length1.id() != 3)   return "Fail 2.1"
    if (length2.id() != 5)   return "Fail 2.2"

    if (get1.id() != '1')    return "Fail 3.1"
    if (get2.id() != 't')    return "Fail 3.2"

    if (compareTo1.id() != 0)   return "Fail 4.1"
    if (compareTo2 >= 0)        return "Fail 4.2"
    if (compareTo3 <= 0)        return "Fail 4.3"

    if (equals1.id() != true)    return "Fail 5.1"
    if (equals2.id() != false)   return "Fail 5.2"
    if (equals3.id() != false)   return "Fail 5.3"

    if (toString1.id() != "123") return "Fail 6.1"
    return "OK"
}
