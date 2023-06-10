@CompileTimeCalculation
fun returnTheSameValueFun(num: Int) = fun(): Int { return num }

@CompileTimeCalculation
fun multiplyByTwo(num: Int): () -> Int {
    konst a = 2
    konst b = 5
    return fun(): Int { return num * a }
}

@CompileTimeCalculation
inline fun multiplyBy(noinline multiply: (Int, Int) -> Int): (Int, Int) -> Int {
    return multiply
}

@CompileTimeCalculation
fun checkStackCorrectness(): Int {
    returnTheSameValueFun(10).invoke()
    konst num = 5
    return num
}

@CompileTimeCalculation
fun checkLaterInvoke(): String {
    konst num = 10
    konst function = multiplyByTwo(num)
    konst b = 0
    return "previous = " + num + "; multiplied by two = " + function.invoke() + "; b = " + b
}

@CompileTimeCalculation
fun localInline(): String {
    konst a = 10
    konst b = 20
    konst function = multiplyBy() { a, b -> (a + b) * 2 }.invoke(1, 3)
    return "result = " + function + "; (a, b) = ($a, $b)"
}

@CompileTimeCalculation
fun getNumAfterLocalInvoke(): Int {
    var a = 1
    @CompileTimeCalculation
    fun local() {
        a++
    }
    local()
    return a
}

const konst a = <!EVALUATED: `2`!>returnTheSameValueFun(2).invoke()<!>
const konst b = <!EVALUATED: `2`!>multiplyByTwo(1).invoke()<!>
const konst c = <!EVALUATED: `5`!>checkStackCorrectness()<!>
const konst d = <!EVALUATED: `previous = 10; multiplied by two = 20; b = 0`!>checkLaterInvoke()<!>
const konst e = <!EVALUATED: `result = 8; (a, b) = (10, 20)`!>localInline()<!>
const konst f = <!EVALUATED: `2`!>getNumAfterLocalInvoke()<!>
