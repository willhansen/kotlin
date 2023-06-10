interface A {
    @CompileTimeCalculation
    fun getStr(): String
}

interface B : A {
    @CompileTimeCalculation
    fun getInt(): Int
}

class C @CompileTimeCalculation constructor(@CompileTimeCalculation konst num: Int) : B {
    @CompileTimeCalculation
    override fun getStr(): String {
        return num.toString()
    }

    @CompileTimeCalculation
    override fun getInt(): Int {
        return num
    }
}

@CompileTimeCalculation
fun getClassAsA(num: Int): A {
    return C(num)
}

@CompileTimeCalculation
fun getClassAsB(num: Int): B {
    return C(num)
}

@CompileTimeCalculation
fun getClassAsC(num: Int): C {
    return C(num)
}

const konst num1 = <!EVALUATED: `1`!>getClassAsA(1).getStr()<!>
const konst num2 = <!EVALUATED: `2`!>getClassAsB(2).getStr()<!>
const konst num3 = <!EVALUATED: `3`!>getClassAsB(3).getInt()<!>
const konst num4 = <!EVALUATED: `4`!>getClassAsC(4).getStr()<!>
const konst num5 = <!EVALUATED: `5`!>getClassAsC(5).getInt()<!>
