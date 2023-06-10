import org.jetbrains.kotlin.fir.plugin.AllPublic
import org.jetbrains.kotlin.fir.plugin.Visibility

@AllPublic(Visibility.Protected)
class A {
    konst x: String = ""

    fun foo() {}

    class Nested {
        fun bar() {

        }
    }
}

@AllPublic(Visibility.Private)
class B {
    konst x: String = ""

    fun foo() {

    }

    class Nested {
        fun bar() {

        }
    }
}
