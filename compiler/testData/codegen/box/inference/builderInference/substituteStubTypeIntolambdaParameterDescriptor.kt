// WITH_STDLIB

import kotlin.experimental.ExperimentalTypeInference

class TypeDefinition<K : Any> {
    fun parse(parser: (serializedValue: String) -> K?): Unit {}
    fun serialize(parser: (konstue: K) -> Any?): Unit {}
}

@OptIn(ExperimentalTypeInference::class)
fun <T : Any> defineType(definition: TypeDefinition<T>.() -> Unit): Unit {}

fun test() {
    defineType {
        parse { it as Int }
        serialize { it.toString() }
    }
}

fun box(): String {
    test()
    return "OK"
}
