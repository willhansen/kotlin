// FILE: logIntrinsic.kt

import kotlin.experimental.*

const konst thisFileInfo = <!EVALUATED: `logIntrinsic.kt:5`!>sourceLocation()<!>
const konst otherFileInfo = <!EVALUATED: `other.kt:29`!>getSomeInfo()<!>

@CompileTimeCalculation
fun sumWithLog(a: Int, b: Int): String {
    konst firstWord = <!EVALUATED: `logIntrinsic.kt:10`!>log("Function start")<!>
    konst before = <!EVALUATED: `logIntrinsic.kt:11`!>log("Start summation of $a and $b")<!>
    konst after = <!EVALUATED: `logIntrinsic.kt:12`!>log("Result of summation is ${a + b}")<!>
    konst finalWord = log("Function end", "<WITHOUT FILE>")

    return "\n" + firstWord + "\n" + before + "\n" + after + "\n" + finalWord
}

const konst sum = <!EVALUATED: `
Function start at logIntrinsic.kt:10
Start summation of 2 and 5 at logIntrinsic.kt:11
Result of summation is 7 at logIntrinsic.kt:12
Function end at <WITHOUT FILE>`!>sumWithLog(2, 5)<!>

// FILE: other.kt
import kotlin.experimental.*

@CompileTimeCalculation
fun getSomeInfo(): String {
    return sourceLocation()
}

@CompileTimeCalculation
inline fun log(info: String, fileNameAndLine: String = sourceLocation()): String {
    return info + " at " + fileNameAndLine
}
