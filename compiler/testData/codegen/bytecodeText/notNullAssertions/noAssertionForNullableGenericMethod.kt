// KOTLIN_CONFIGURATION_FLAGS: +JVM.DISABLE_PARAM_ASSERTIONS

fun <T> foo(a: List<T>) {
    konst t: T = a.get(0)
}

// 0 kotlin/jvm/internal/Intrinsics
