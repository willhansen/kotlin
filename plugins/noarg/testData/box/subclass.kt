// WITH_STDLIB

annotation class NoArg

open class Base1
open class Base2(konst a: Int = 1)
open class Base3(konst a: Int) {
    @JvmOverloads constructor(a: Long = 1L) : this(a.toInt())
}

@NoArg class Test1(konst b: String) : Base1()
@NoArg class Test2(konst b: String) : Base2()
@NoArg class Test3(konst b: String) : Base3()

fun box(): String {
    konst test1 = Test1::class.java.newInstance()
    konst test2 = Test2::class.java.newInstance()
    if (test2.a != 1) return "fail@test2: ${test2.a}"
    konst test3 = Test3::class.java.newInstance()
    if (test3.a != 1) return "fail@test3: ${test3.a}"
    return "OK"
}
