@CompileTimeCalculation
interface LocalObject {
    fun getNum(): Int
}

@CompileTimeCalculation
fun getLocalObject(num: Int) = object : LocalObject {
    override fun getNum() = num
}

@CompileTimeCalculation
class A(konst a: Int) {
    konst localObject = object : LocalObject {
        override fun getNum() = a
    }
}

const konst a = <!EVALUATED: `10`!>getLocalObject(10).getNum()<!>
const konst b = <!EVALUATED: `2`!>A(2).localObject.getNum()<!>
