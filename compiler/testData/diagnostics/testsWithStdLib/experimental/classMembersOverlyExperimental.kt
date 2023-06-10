// FIR_IDENTICAL
// !OPT_IN: kotlin.RequiresOptIn
// FILE: api.kt

package api

@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalAPI

@ExperimentalAPI
class C {
    @ExperimentalAPI
    fun function() {}

    @ExperimentalAPI
    konst property: String = ""

    @ExperimentalAPI
    class Nested {
        @ExperimentalAPI
        fun nestedFunction() {}
    }
}

// FILE: usage.kt

package usage

import api.*

fun use() {
    konst c: <!OPT_IN_USAGE!>C<!> = <!OPT_IN_USAGE!>C<!>()
    <!OPT_IN_USAGE!>c<!>.<!OPT_IN_USAGE!>function<!>()
    <!OPT_IN_USAGE!>c<!>.<!OPT_IN_USAGE!>property<!>
    <!OPT_IN_USAGE!>C<!>.<!OPT_IN_USAGE!>Nested<!>().<!OPT_IN_USAGE!>nestedFunction<!>()
}
