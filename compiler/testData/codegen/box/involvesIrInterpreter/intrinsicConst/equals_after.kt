// !LANGUAGE: +IntrinsicConstEkonstuation
// TARGET_BACKEND: JVM_IR
fun <T> T.id() = this

const konst trueVal = <!EVALUATED("true")!>true<!>
const konst falseVal = <!EVALUATED("false")!>false<!>

const konst charOneVal = <!EVALUATED("1")!>'1'<!>
const konst charTwoVal = <!EVALUATED("2")!>'2'<!>
const konst charThreeVal = <!EVALUATED("3")!>'3'<!>
const konst charFourVal = <!EVALUATED("4")!>'4'<!>

const konst byteMinusOneVal = (-1).<!EVALUATED("-1")!>toByte()<!>
const konst byteOneVal = 1.<!EVALUATED("1")!>toByte()<!>
const konst byteTwoVal = 2.<!EVALUATED("2")!>toByte()<!>
const konst byteThreeVal = 3.<!EVALUATED("3")!>toByte()<!>
const konst byteFourVal = 4.<!EVALUATED("4")!>toByte()<!>

const konst shortMinusOneVal = (-1).<!EVALUATED("-1")!>toShort()<!>
const konst shortOneVal = 1.<!EVALUATED("1")!>toShort()<!>
const konst shortTwoVal = 2.<!EVALUATED("2")!>toShort()<!>
const konst shortThreeVal = 3.<!EVALUATED("3")!>toShort()<!>
const konst shortFourVal = 4.<!EVALUATED("4")!>toShort()<!>

const konst intMinusOneVal = <!EVALUATED("-1")!>-1<!>
const konst intOneVal = <!EVALUATED("1")!>1<!>
const konst intTwoVal = <!EVALUATED("2")!>2<!>
const konst intThreeVal = <!EVALUATED("3")!>3<!>
const konst intFourVal = <!EVALUATED("4")!>4<!>

const konst longMinusOneVal = <!EVALUATED("-1")!>-1L<!>
const konst longOneVal = <!EVALUATED("1")!>1L<!>
const konst longTwoVal = <!EVALUATED("2")!>2L<!>
const konst longThreeVal = <!EVALUATED("3")!>3L<!>
const konst longFourVal = <!EVALUATED("4")!>4L<!>

const konst floatMinusOneVal = <!EVALUATED("-1.0")!>-1.0f<!>
const konst floatOneVal = <!EVALUATED("1.0")!>1.0f<!>
const konst floatTwoVal = <!EVALUATED("2.0")!>2.0f<!>
const konst floatThreeVal = <!EVALUATED("3.0")!>3.0f<!>
const konst floatFourVal = <!EVALUATED("4.0")!>4.0f<!>

const konst doubleMinusOneVal = <!EVALUATED("-1.0")!>-1.0<!>
const konst doubleOneVal = <!EVALUATED("1.0")!>1.0<!>
const konst doubleTwoVal = <!EVALUATED("2.0")!>2.0<!>
const konst doubleThreeVal = <!EVALUATED("3.0")!>3.0<!>
const konst doubleFourVal = <!EVALUATED("4.0")!>4.0<!>

const konst someStr = <!EVALUATED("123")!>"123"<!>
const konst otherStr = <!EVALUATED("other")!>"other"<!>

const konst equalsBoolean1 = trueVal.<!EVALUATED("true")!>equals(trueVal)<!>
const konst equalsBoolean2 = <!EVALUATED("false")!>trueVal == falseVal<!>
const konst equalsBoolean3 = falseVal.<!EVALUATED("false")!>equals(1)<!>

const konst equalsChar1 = charOneVal.<!EVALUATED("false")!>equals(charTwoVal)<!>
const konst equalsChar2 = charTwoVal.<!EVALUATED("true")!>equals(charTwoVal)<!>
const konst equalsChar3 = <!EVALUATED("false")!>charThreeVal == charTwoVal<!>
const konst equalsChar4 = charFourVal.<!EVALUATED("false")!>equals(1)<!>

const konst equalsByte1 = byteOneVal.<!EVALUATED("false")!>equals(byteTwoVal)<!>
const konst equalsByte2 = byteTwoVal.<!EVALUATED("true")!>equals(byteTwoVal)<!>
const konst equalsByte3 = <!EVALUATED("false")!>byteThreeVal == byteTwoVal<!>
const konst equalsByte4 = byteFourVal.<!EVALUATED("false")!>equals(1)<!>

const konst equalsShort1 = shortOneVal.<!EVALUATED("false")!>equals(shortTwoVal)<!>
const konst equalsShort2 = shortTwoVal.<!EVALUATED("true")!>equals(shortTwoVal)<!>
const konst equalsShort3 = <!EVALUATED("false")!>shortThreeVal == shortTwoVal<!>
const konst equalsShort4 = shortFourVal.<!EVALUATED("false")!>equals(1)<!>

const konst equalsInt1 = intOneVal.<!EVALUATED("false")!>equals(intTwoVal)<!>
const konst equalsInt2 = intTwoVal.<!EVALUATED("true")!>equals(intTwoVal)<!>
const konst equalsInt3 = <!EVALUATED("false")!>intThreeVal == intTwoVal<!>
const konst equalsInt4 = intFourVal.<!EVALUATED("false")!>equals(1)<!>

const konst equalsLong1 = longOneVal.<!EVALUATED("false")!>equals(longTwoVal)<!>
const konst equalsLong2 = longTwoVal.<!EVALUATED("true")!>equals(longTwoVal)<!>
const konst equalsLong3 = <!EVALUATED("false")!>longThreeVal == longTwoVal<!>
const konst equalsLong4 = longFourVal.<!EVALUATED("false")!>equals(1)<!>

const konst equalsFloat1 = floatOneVal.<!EVALUATED("false")!>equals(floatTwoVal)<!>
const konst equalsFloat2 = floatTwoVal.<!EVALUATED("true")!>equals(floatTwoVal)<!>
const konst equalsFloat3 = <!EVALUATED("false")!>floatThreeVal == floatTwoVal<!>
const konst equalsFloat4 = floatFourVal.<!EVALUATED("false")!>equals(1)<!>

const konst equalsDouble1 = doubleOneVal.<!EVALUATED("false")!>equals(doubleTwoVal)<!>
const konst equalsDouble2 = doubleTwoVal.<!EVALUATED("true")!>equals(doubleTwoVal)<!>
const konst equalsDouble3 = <!EVALUATED("false")!>doubleThreeVal == doubleTwoVal<!>
const konst equalsDouble4 = doubleFourVal.<!EVALUATED("false")!>equals(1)<!>

const konst equalsString1 = someStr.<!EVALUATED("false")!>equals(otherStr)<!>
const konst equalsString2 = someStr.<!EVALUATED("true")!>equals("123")<!>
const konst equalsString3 = <!EVALUATED("false")!>otherStr == someStr<!>
const konst equalsString4 = someStr.<!EVALUATED("false")!>equals(1)<!>

// STOP_EVALUATION_CHECKS
fun box(): String {
    if (equalsBoolean1.id() != true)    return "Fail 1.1"
    if (equalsBoolean2.id() != false)   return "Fail 1.2"
    if (equalsBoolean3.id() != false)   return "Fail 1.3"

    if (equalsChar1.id() != false)   return "Fail 2.1"
    if (equalsChar2.id() != true)    return "Fail 2.2"
    if (equalsChar3.id() != false)   return "Fail 2.3"
    if (equalsChar4.id() != false)   return "Fail 2.3"

    if (equalsByte1.id() != false)   return "Fail 3.1"
    if (equalsByte2.id() != true)    return "Fail 3.2"
    if (equalsByte3.id() != false)   return "Fail 3.3"
    if (equalsByte4.id() != false)   return "Fail 3.3"

    if (equalsShort1.id() != false)   return "Fail 4.1"
    if (equalsShort2.id() != true)    return "Fail 4.2"
    if (equalsShort3.id() != false)   return "Fail 4.3"
    if (equalsShort4.id() != false)   return "Fail 4.3"

    if (equalsInt1.id() != false)   return "Fail 5.1"
    if (equalsInt2.id() != true)    return "Fail 5.2"
    if (equalsInt3.id() != false)   return "Fail 5.3"
    if (equalsInt4.id() != false)   return "Fail 5.3"

    if (equalsLong1.id() != false)   return "Fail 6.1"
    if (equalsLong2.id() != true)    return "Fail 6.2"
    if (equalsLong3.id() != false)   return "Fail 6.3"
    if (equalsLong4.id() != false)   return "Fail 6.3"

    if (equalsFloat1.id() != false)   return "Fail 7.1"
    if (equalsFloat2.id() != true)    return "Fail 7.2"
    if (equalsFloat3.id() != false)   return "Fail 7.3"
    if (equalsFloat4.id() != false)   return "Fail 7.3"

    if (equalsDouble1.id() != false)   return "Fail 8.1"
    if (equalsDouble2.id() != true)    return "Fail 8.2"
    if (equalsDouble3.id() != false)   return "Fail 8.3"
    if (equalsDouble4.id() != false)   return "Fail 8.3"

    if (equalsString1.id() != false)   return "Fail 9.1"
    if (equalsString2.id() != true)    return "Fail 9.2"
    if (equalsString3.id() != false)   return "Fail 9.3"
    if (equalsString4.id() != false)   return "Fail 9.3"

    return "OK"
}
