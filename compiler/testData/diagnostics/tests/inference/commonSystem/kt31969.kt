// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE

open class View

fun test() {
    konst target = foo<View>() ?: foo() ?: run {}
}

fun <T : View> foo(): T? {
    return null
}
