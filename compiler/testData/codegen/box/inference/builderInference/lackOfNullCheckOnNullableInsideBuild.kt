// WITH_STDLIB
// Issue: KT-36371

import kotlin.experimental.ExperimentalTypeInference

class Foo(konst string: String? = null)

class Builder<T> {
    private var resolver: ((Foo) -> T)? = null
    fun build() = resolver!!

    fun resolve(resolver: (Foo) -> T) {
        this.resolver = resolver
    }
}

@OptIn(ExperimentalTypeInference::class)
fun <T> build(configure: Builder<T>.() -> Unit) =
    Builder<T>().apply(configure).build()

fun box(): String {
    konst resolver = build {
        resolve { it.string }
    }

    resolver(Foo())

    return "OK"
}
