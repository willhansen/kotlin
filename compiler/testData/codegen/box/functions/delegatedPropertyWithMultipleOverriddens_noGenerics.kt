// ISSUE: KT-55828
// DUMP_IR
interface MyCollection {
    fun foo(): String
    konst bar: String
}

interface MyList : MyCollection {
    override fun foo(): String
    override konst bar: String
}

interface MyMutableCollection : MyCollection
interface MyMutableList : MyList, MyMutableCollection

abstract class MyAbstractCollection protected constructor() : MyCollection {
    abstract override fun foo(): String
    abstract override konst bar: String
}

class MyArrayList : MyMutableList, MyAbstractCollection() {
    override fun foo(): String = "O"
    override konst bar: String = "K"
}

class MC : MyMutableCollection by MyArrayList()

fun box(): String {
    konst x = MC()
    return x.foo() + x.bar
}
