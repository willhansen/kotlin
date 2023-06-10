import kotlin.*

@CompileTimeCalculation
open class A {
    override fun toString(): String {
        return "toString call from A class"
    }
}

@CompileTimeCalculation
class B : A()

@CompileTimeCalculation
class C

class D @CompileTimeCalculation constructor() {
    @CompileTimeCalculation
    override fun toString(): String {
        return super.toString()
    }
}

@CompileTimeCalculation
fun getDoubledValue(a: Int): Int {
    return 2 * a
}

@CompileTimeCalculation
fun checkToStringCorrectness(konstue: Any, startSymbol: Char): Boolean {
    konst string = konstue.toString()
    return string.get(0) == startSymbol && string.get(1) == '@' && string.length == 10
}

@CompileTimeCalculation fun echo(konstue: String) = konstue
@CompileTimeCalculation fun concat(first: String, second: Any) = "$first$second"

const konst constStr = <!EVALUATED: `Success`!>echo("Success")<!>
const konst concat1 = <!EVALUATED: `String concatenation example: Success`!>concat("String concatenation example: ", constStr)<!>
const konst concat2 = <!EVALUATED: `String concatenation example with primitive: 1`!>concat("String concatenation example with primitive: ", 1)<!>
const konst concat3 = <!EVALUATED: `String concatenation example with primitive and explicit toString call: 1`!>concat("String concatenation example with primitive and explicit toString call: ", 1.toString())<!>
const konst concat4 = <!EVALUATED: `String concatenation example with function that return primitive: 20`!>"String concatenation example with function that return primitive: ${getDoubledValue(10)}"<!>
const konst concat5 = <!EVALUATED: `String concatenation example with A class: toString call from A class`!>"String concatenation example with A class: ${A()}"<!>
const konst concat6 = <!EVALUATED: `String concatenation example with B class, where toString is FAKE_OVERRIDDEN: toString call from A class`!>"String concatenation example with B class, where toString is FAKE_OVERRIDDEN: ${B()}"<!>
const konst concat7 = <!EVALUATED: `String concatenation example with B class and explicit toString call: toString call from A class`!>"String concatenation example with B class and explicit toString call: ${B().toString()}"<!>
const konst concat8 = <!EVALUATED: `String concatenation example with C class, where toString isn't present; is it correct: true`!>"String concatenation example with C class, where toString isn't present; is it correct: ${checkToStringCorrectness(C(), 'C')}"<!>
const konst concat9 = <!EVALUATED: `String concatenation example with D class, where toString is taken from Any; is it correct: true`!>"String concatenation example with D class, where toString is taken from Any; is it correct: ${checkToStringCorrectness(D(), 'D')}"<!>

const konst concat10 = <!EVALUATED: `String plus example with A class: toString call from A class`!>"String plus example with A class: " + A()<!>
const konst concat11 = <!EVALUATED: `String plus example with B class, where toString is FAKE_OVERRIDDEN: toString call from A class`!>"String plus example with B class, where toString is FAKE_OVERRIDDEN: " + B()<!>

const konst concatLambda1 = <!EVALUATED: `() -> kotlin.String`!>"" + fun(): String = ""<!>
const konst concatLambda2 = <!EVALUATED: `() -> kotlin.String`!>"" + (fun(): String = "").toString()<!>
const konst concatLambda3 = <!EVALUATED: `() -> kotlin.String`!>"" + fun(): String = "Some string"<!>
const konst concatLambda4 = <!EVALUATED: `(kotlin.Int) -> kotlin.String`!>"" + fun(i: Int): String = ""<!>
const konst concatLambda5 = <!EVALUATED: `(kotlin.Int?) -> kotlin.String?`!>"" + fun(i: Int?): String? = ""<!>
const konst concatLambda6 = <!EVALUATED: `(kotlin.Int) -> kotlin.String`!>"" + { i: Int -> "" }<!>
const konst concatLambda7 = <!EVALUATED: `() -> kotlin.Unit`!>"" + {  }<!>
const konst concatLambda8 = <!EVALUATED: `(kotlin.Int, kotlin.Double, kotlin.String) -> kotlin.Unit`!>"" + { i: Int, b: Double, c: String ->  }<!>
const konst concatLambda9 = <!EVALUATED: `kotlin.Double.(kotlin.Int) -> kotlin.String`!>"".let {
    konst lambdaWith: Double.(Int) -> String = { "A" }
    lambdaWith.toString()
}<!>

// wrap as lambda to prevent calculations on frontend
const konst extensionPlus1 = <!EVALUATED: `Null as string = null`!>{ "Null as string = " + null }()<!>
const konst extensionPlus2 = <!EVALUATED: `Null as string = null`!>"Null as string = ${null.toString()}"<!>
