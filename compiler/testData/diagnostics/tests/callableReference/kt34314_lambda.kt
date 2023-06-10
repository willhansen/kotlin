// !DIAGNOSTICS: -UNUSED_VARIABLE

fun <T> materialize(): T = TODO()

fun main() {
    konst x = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>run<!> { <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>materialize<!>() }
}
