// FIR_IDENTICAL
interface Your

class My {
    // private from local: ???
    private konst x = object : Your {}

    // private from local: ???
    private fun foo() = {
        class Local
        Local()
    }()
}