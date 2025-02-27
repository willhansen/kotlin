// IGNORE_BACKEND_K1: JVM_IR
package test

import kotlin.reflect.KFunction
import kotlin.collections.*

@CompileTimeCalculation
fun withParameters(a: Int, b: Double) = 0
@CompileTimeCalculation
fun String.withExtension(a: Int) = 0

@CompileTimeCalculation
class A {
    fun String.get(a: Int) = this
}

const konst parameters1 = <!EVALUATED: `parameter #0 a of fun withParameters(kotlin.Int, kotlin.Double): kotlin.Int, parameter #1 b of fun withParameters(kotlin.Int, kotlin.Double): kotlin.Int`!>(::withParameters as KFunction<*>).parameters.joinToString()<!>
const konst parameters2 = <!EVALUATED: `extension receiver parameter of fun kotlin.String.withExtension(kotlin.Int): kotlin.Int, parameter #1 a of fun kotlin.String.withExtension(kotlin.Int): kotlin.Int`!>(String::withExtension as KFunction<*>).parameters.joinToString()<!>
const konst parameters3 = <!EVALUATED: `instance parameter of fun test.A.(kotlin.String.)get(kotlin.Int): kotlin.String, extension receiver parameter of fun test.A.(kotlin.String.)get(kotlin.Int): kotlin.String, parameter #2 a of fun test.A.(kotlin.String.)get(kotlin.Int): kotlin.String`!>A::class.members.toList()[0].parameters.joinToString()<!>

// properties
@CompileTimeCalculation
class B(konst b: Int) {
    konst String.size: Int
        get() = this.length
}

const konst property0Parameters = <!EVALUATED: `[]`!>B(1)::b.parameters.toString()<!>
const konst property1Parameters = <!EVALUATED: `[instance parameter of konst test.B.b: kotlin.Int]`!>B::b.parameters.toString()<!>
const konst property2Parameters = <!EVALUATED: `[instance parameter of konst test.B.(kotlin.String.)size: kotlin.Int, extension receiver parameter of konst test.B.(kotlin.String.)size: kotlin.Int]`!>B::class.members.toList()[1].parameters.toString()<!>
