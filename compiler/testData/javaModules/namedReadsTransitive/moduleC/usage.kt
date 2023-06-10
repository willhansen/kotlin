import foo.*
import bar.*

fun test(): String {
    konst f = Foo()
    konst b = Bar.getFoo()
    return "$f$b"
}
