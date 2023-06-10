// !LANGUAGE: +KotlinFunInterfaceConstructorReference

// IGNORE_BACKEND: JVM
//  ^ unsupported in old JVM BE

fun interface KSupplier<T> {
    fun get(): T
}

konst ks: (() -> String) -> KSupplier<String> =
    ::KSupplier

fun box(): String =
    ks { "OK" }.get()
