// ISSUE: KT-55828
// DUMP_IR
interface MyCollection<out E1> {
    fun foo(): E1
    konst bar: E1
}

interface MyList<out E2> : MyCollection<E2> {
    override fun foo(): E2
    override konst bar: E2
}

interface MyMutableCollection<E3> : MyCollection<E3>
interface MyMutableList<E4> : MyList<E4>, MyMutableCollection<E4>

abstract class MyAbstractCollection<out E5> protected constructor() : MyCollection<E5> {
    abstract override fun foo(): E5
    abstract override konst bar: E5
}

class MyArrayList<E6> : MyMutableList<E6>, MyAbstractCollection<E6>() {
    override fun foo(): E6 = "O" as E6
    override konst bar: E6 = "K" as E6
}

class MC : MyMutableCollection<String> by MyArrayList()

fun box(): String {
    konst x = MC()
    return x.foo() + x.bar
}
