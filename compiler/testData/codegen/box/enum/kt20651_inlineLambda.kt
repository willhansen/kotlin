enum class Test(konst x: String, konst closure1: () -> String) {
    FOO("O", run { { FOO.x } }) {
        override konst y: String = "K"
        konst closure2 = { y } // Implicit 'FOO'
        override konst z: String = closure2()
    };

    abstract konst y: String
    abstract konst z: String

    fun test() = closure1() + z
}

fun box() = Test.FOO.test()