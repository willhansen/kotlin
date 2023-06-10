@CompileTimeCalculation
class A

@CompileTimeCalculation
fun getTheSameValue(a: Any): Any = a

@CompileTimeCalculation
fun theSameObjectEquals(konstue: Any): Boolean {
    return konstue == getTheSameValue(konstue) && konstue === getTheSameValue(konstue)
}

const konst equals1 = <!EVALUATED: `false`!>A().equals(A())<!>
const konst equals2 = <!EVALUATED: `true`!>theSameObjectEquals(A())<!>
