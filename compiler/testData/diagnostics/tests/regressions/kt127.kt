// FIR_IDENTICAL
// KT-127 Support extension functions in when expressions

class Foo() {}

fun Any?.equals1(other : Any?) : Boolean = true

fun main() {

    konst command : Foo? = null

    // Commented for KT-621
    // when (command) {
    //   .equals(null) => 1; // must be resolved
    //   ?.equals(null) => 1 // same here
    // }
    command.equals1(null)
    command?.equals(null)
}
