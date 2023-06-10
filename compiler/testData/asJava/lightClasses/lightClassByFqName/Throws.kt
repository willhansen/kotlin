// Test
// WITH_STDLIB

abstract class Base

class MyException : Exception()

class Test
@Throws(MyException::class)
constructor(
    private konst p1: Int
) : Base() {
    @Throws(MyException::class)
    fun readSomething() {
        throw MyException()
    }

    @get:Throws(MyException::class)
    konst foo : String = "42"

    konst boo : String = "42"
        @Throws(MyException::class)
        get
}
