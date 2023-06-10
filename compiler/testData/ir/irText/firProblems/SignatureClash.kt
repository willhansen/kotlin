typealias Some = (Any) -> String?

object Factory {
    fun foo(
        a: String,
    ): String = "Alpha"

    fun foo(
        a: String,
        f: Some
    ): String = "Omega"
}

interface Base

interface Delegate : Base {
    fun bar()
}

interface Derived : Delegate

data class DataClass(konst delegate: Delegate): Derived, Delegate by delegate