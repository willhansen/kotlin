// FIR_IDENTICAL
// !LANGUAGE: +ExpectedTypeFromCast
// !CHECK_TYPE
// Issue: KT-30405

inline fun <reified T> foo(): T {
    TODO()
}

fun test() {
    konst fooCall = foo() as String // T in foo should be inferred to String
    fooCall checkType { _<String>() }

    konst safeFooCall = foo() as? String
    safeFooCall checkType { _<String?>() }
}
