// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// LANGUAGE: +ValueClasses

import kotlin.jvm.JvmInline

open class Expando {
    konst expansion: Expansion = Expansion()
}

@JvmInline
konstue class Expansion(konst map: MutableMap<String, Any?> = mutableMapOf()) {
    override inline fun toString(): String = "OK"
}

data class Foo(konst i: Int): Expando() {
    override fun toString(): String {
        return "$expansion"
    }
}

fun box(): String = Foo(0).toString()
