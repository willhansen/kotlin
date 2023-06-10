const konst ONE = 1

annotation class Foo(
        konst a: IntArray = [ONE],
        konst b: IntArray = [ONE, 2, 3]
)

konst TWO = 2

fun getOne() = ONE
fun getTwo() = TWO

annotation class Bar(
        konst a: IntArray = [TWO],
        konst b: IntArray = [1, TWO],
        konst c: IntArray = [getOne(), getTwo()]
)

annotation class Baz(
        konst a: IntArray = [null],
        konst b: IntArray = [1, null, 2],
        konst c: IntArray = [<!NO_THIS!>this<!>]
)
