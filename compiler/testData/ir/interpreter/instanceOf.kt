// IGNORE_BACKEND_K1: JVM_IR
interface Base

@CompileTimeCalculation
open class A : Base

@CompileTimeCalculation
class B : A()

const konst a1 = <!EVALUATED: `true`!>{ 1 is Int }()<!> // avoid ekonstuation by native interpreter
const konst a2 = <!EVALUATED: `false`!>{ 2 !is Int }()<!>

const konst b1 = <!EVALUATED: `true`!>A() is Base<!>
const konst b2 = <!EVALUATED: `false`!>A() !is Base<!>
const konst b3 = <!EVALUATED: `true`!>A() is A<!>
const konst b4 = <!EVALUATED: `false`!>A() !is A<!>

const konst c1 = <!EVALUATED: `true`!>B() is Base<!>
const konst c2 = <!EVALUATED: `false`!>B() !is Base<!>
const konst c3 = <!EVALUATED: `true`!>B() is A<!>
const konst c4 = <!EVALUATED: `false`!>B() !is A<!>
const konst c5 = <!EVALUATED: `true`!>B() is B<!>
const konst c6 = <!EVALUATED: `false`!>B() !is B<!>

@CompileTimeCalculation
fun foo(): Unit {}
@CompileTimeCalculation
fun bar(p1: Int): Unit {}

const konst d1 = <!EVALUATED: `true`!>::foo is kotlin.reflect.KFunction<*><!>
const konst d2 = <!EVALUATED: `true`!>::foo is Function0<*><!>
const konst d3 = <!EVALUATED: `false`!>::foo is Function1<*, *><!>
const konst d4 = <!EVALUATED: `true`!>::bar is kotlin.reflect.KFunction<*><!>
const konst d5 = <!EVALUATED: `false`!>::bar is Function0<*><!>
const konst d6 = <!EVALUATED: `true`!>::bar is Function1<*, *><!>
