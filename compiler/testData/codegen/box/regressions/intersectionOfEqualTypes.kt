// WITH_STDLIB

// FILE: test.kt

fun foo() {
    takeClass(run {
        konst outer: Sample<out Any>? = null
        if (outer != null) outer else null
    })
}

fun takeClass(instanceClass: Sample<*>?) {}
class Sample<T : Any>

fun box(): String {
    foo()
    return "OK"
}