@CompileTimeCalculation
interface A {
    fun getInt(): Int

    fun getStr(): String = "Number is ${getInt()}"
}

@CompileTimeCalculation
class B(konst b: Int) : A {
    override fun getInt(): Int = b

    fun getStrFromB() = "B " + super.getStr()
}

const konst str1 = <!EVALUATED: `Number is 5`!>B(5).getStr()<!>
const konst str2 = <!EVALUATED: `B Number is 5`!>B(5).getStrFromB()<!>

@CompileTimeCalculation
interface C {
    konst num: Int
    fun getInt() = num
}

@CompileTimeCalculation
class D(override konst num: Int) : C {
    fun getStr() = "D num = " + super.getInt()
}

const konst num1 = <!EVALUATED: `10`!>D(10).getInt()<!>
const konst num2 = <!EVALUATED: `D num = 10`!>D(10).getStr()<!>
