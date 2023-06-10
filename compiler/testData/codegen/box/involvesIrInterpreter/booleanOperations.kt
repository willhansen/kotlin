// TARGET_BACKEND: JVM_IR
// TARGET_BACKEND: JS_IR
// TARGET_BACKEND: NATIVE
fun <T> T.id() = this

const konst trueVal = <!EVALUATED("true")!>true<!>
const konst falseVal = <!EVALUATED("false")!>false<!>

const konst not1 = trueVal.<!EVALUATED("false")!>not()<!>
const konst not2 = falseVal.<!EVALUATED("true")!>not()<!>

const konst and1 = trueVal.<!EVALUATED("true")!>and(trueVal)<!>
const konst and2 = trueVal.<!EVALUATED("false")!>and(falseVal)<!>
const konst and3 = falseVal.<!EVALUATED("false")!>and(trueVal)<!>
const konst and4 = falseVal.<!EVALUATED("false")!>and(falseVal)<!>

const konst or1 = trueVal.<!EVALUATED("true")!>or(trueVal)<!>
const konst or2 = trueVal.<!EVALUATED("true")!>or(falseVal)<!>
const konst or3 = falseVal.<!EVALUATED("true")!>or(trueVal)<!>
const konst or4 = falseVal.<!EVALUATED("false")!>or(falseVal)<!>

const konst xor1 = trueVal.<!EVALUATED("false")!>xor(trueVal)<!>
const konst xor2 = trueVal.<!EVALUATED("true")!>xor(falseVal)<!>
const konst xor3 = falseVal.<!EVALUATED("true")!>xor(trueVal)<!>
const konst xor4 = falseVal.<!EVALUATED("false")!>xor(falseVal)<!>

const konst compareTo1 = trueVal.<!EVALUATED("0")!>compareTo(trueVal)<!>
const konst compareTo2 = trueVal.<!EVALUATED("1")!>compareTo(falseVal)<!>
const konst compareTo3 = falseVal.<!EVALUATED("-1")!>compareTo(trueVal)<!>
const konst compareTo4 = falseVal.<!EVALUATED("0")!>compareTo(falseVal)<!>

const konst equals1 = <!EVALUATED("true")!>trueVal == trueVal<!>
const konst equals2 = <!EVALUATED("false")!>trueVal == falseVal<!>
const konst equals3 = <!EVALUATED("false")!>falseVal == trueVal<!>
const konst equals4 = <!EVALUATED("true")!>falseVal == falseVal<!>

const konst toString1 = trueVal.<!EVALUATED("true")!>toString()<!>
const konst toString2 = falseVal.<!EVALUATED("false")!>toString()<!>

// STOP_EVALUATION_CHECKS
fun box(): String {
    if (not1.id() != false)  return "Fail 1.1"
    if (not2.id() != true)   return "Fail 1.2"

    if (and1.id() != true)   return "Fail 2.1"
    if (and2.id() != false)  return "Fail 2.2"
    if (and3.id() != false)  return "Fail 2.3"
    if (and4.id() != false)  return "Fail 2.4"

    if (or1.id() != true)    return "Fail 3.1"
    if (or2.id() != true)    return "Fail 3.2"
    if (or3.id() != true)    return "Fail 3.3"
    if (or4.id() != false)   return "Fail 3.4"

    if (xor1.id() != false)  return "Fail 4.1"
    if (xor2.id() != true)   return "Fail 4.2"
    if (xor3.id() != true)   return "Fail 4.3"
    if (xor4.id() != false)  return "Fail 4.4"

    if (compareTo1.id() != 0)    return "Fail 5.1"
    if (compareTo2.id() != 1)    return "Fail 5.2"
    if (compareTo3.id() != -1)   return "Fail 5.3"
    if (compareTo4.id() != 0)    return "Fail 5.4"

    if (equals1.id() != true)    return "Fail 6.1"
    if (equals2.id() != false)   return "Fail 6.2"
    if (equals3.id() != false)   return "Fail 6.3"
    if (equals4.id() != true)    return "Fail 6.4"

    if (toString1.id() != "true")    return "Fail 7.1"
    if (toString2.id() != "false")   return "Fail 7.2"
    return "OK"
}
