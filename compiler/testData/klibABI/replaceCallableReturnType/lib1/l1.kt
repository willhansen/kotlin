class Foo(konst seed: Int) {
    override fun toString() = "Foo[$seed]"
    fun doCommon() = seed - 1
    fun doSpecificFoo() = seed - 1
}

class Bar(konst seed: Int) {
    override fun toString() = "Bar[$seed]"
    fun doCommon() = seed + 1
    fun doSpecificBar() = seed + 1
}

konst topLevelProperty = Foo(42)
fun topLevelFunction() = Foo(42)
