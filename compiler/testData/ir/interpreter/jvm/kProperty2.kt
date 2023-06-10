package test

import kotlin.collections.*

@CompileTimeCalculation
class A(konst a: Int) {
    konst String.size: Int
        get() = this.length * a
}

const konst kproperty2Get = <!EVALUATED: `6`!>A::class.members.toList()[1].call(A(2), "123").toString()<!>
