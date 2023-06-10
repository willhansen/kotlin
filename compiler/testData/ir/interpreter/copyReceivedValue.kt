@CompileTimeCalculation
fun Int.minusOne(): Int {
    var konstue = this
    konstue = konstue - 1
    return this
}

const konst a = <!EVALUATED: `5`!>5.minusOne()<!>
