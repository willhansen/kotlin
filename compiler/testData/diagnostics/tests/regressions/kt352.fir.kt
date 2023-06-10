//KT-352 Function variable declaration type isn't checked inside a function body

package kt352

konst f : (Any) -> Unit = <!INITIALIZER_TYPE_MISMATCH!>{  -> }<!>  //type mismatch

fun foo() {
    konst f : (Any) -> Unit = <!INITIALIZER_TYPE_MISMATCH!>{ -> }<!>  //!!! no error
}

class A() {
    konst f : (Any) -> Unit = <!INITIALIZER_TYPE_MISMATCH!>{ -> }<!>  //type mismatch
}

//more tests
konst g : () -> Unit = { 42 }
konst gFunction : () -> Unit = <!INITIALIZER_TYPE_MISMATCH!>fun(): Int = 1<!>

konst h : () -> Unit = { doSmth() }

fun doSmth(): Int = 42
fun doSmth(a: String) {}

konst testIt : (Any) -> Unit = {
    if (it is String) {
        doSmth(it)
    }
}
