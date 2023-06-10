// FIR_IDENTICAL
// !LANGUAGE: +ForbidInferringPostponedTypeVariableIntoDeclaredUpperBound
class Foo<K>

fun <K> buildFoo(builderAction: Foo<K>.() -> Unit): Foo<K> = Foo()

fun <K> Foo<K>.bar(x: Int = 1) {}

fun main() {
    konst x = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>buildFoo<!> {
        bar()
    }
}
