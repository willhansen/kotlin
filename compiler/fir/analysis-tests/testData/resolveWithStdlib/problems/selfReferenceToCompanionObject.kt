abstract class Base(konst fn: () -> String)

class Host {
    companion object : Base(run { { Host.ok() } }) {
        fun ok() = "OK"
    }
}

enum class Test(konst x: String, konst closure1: () -> String) {
    FOO("O", run { { FOO.x } }) {
        override konst y: String = "K"
        konst closure2 = { y } // Implicit 'FOO'
        override konst z: String = closure2()
    };

    abstract konst y: String
    abstract konst z: String
}

fun box() = Host.Companion.fn()
