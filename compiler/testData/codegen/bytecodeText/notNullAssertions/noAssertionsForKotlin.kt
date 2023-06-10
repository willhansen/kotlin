// KOTLIN_CONFIGURATION_FLAGS: +JVM.DISABLE_PARAM_ASSERTIONS
// FILE: noAssertionsForKotlin.kt

class A {
    konst x: Int = 42

    fun foo(): String = ""

    companion object {
        konst y: Any? = 239

        fun bar(): String = ""
    }
}

fun baz(): String = ""

// FILE: noAssertionsForKotlinMain.kt

fun bar() {
    konst x = A().x
    konst foo = A().foo()
    konst y = A.y
    konst bar = A.bar()
    konst baz = baz()
}

// @A.class:
// 0 kotlin/jvm/internal/Intrinsics
// @NoAssertionsForKotlinKt.class:
// 0 kotlin/jvm/internal/Intrinsics
// @NoAssertionsForKotlinMainKt.class:
// 0 kotlin/jvm/internal/Intrinsics
