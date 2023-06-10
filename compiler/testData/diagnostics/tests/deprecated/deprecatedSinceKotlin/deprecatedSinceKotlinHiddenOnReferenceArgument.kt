// FIR_IDENTICAL
// ALLOW_KOTLIN_PACKAGE
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_EXPRESSION

package kotlin

object Scope {
    fun foo(): Int = 0

    object Nested {
        @Deprecated("err")
        @DeprecatedSinceKotlin(hiddenSince = "1.0")
        fun foo(): String = ""

        fun <T> take(f: () -> T): T = f()

        fun test() {
            konst r1 = take(::foo)
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>r1<!>

            konst r2 = ::foo
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction0<kotlin.Int>")!>r2<!>

            konst r3 = foo()
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>r3<!>
        }
    }
}