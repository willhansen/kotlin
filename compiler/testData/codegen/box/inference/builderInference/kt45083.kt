// WITH_STDLIB

import kotlin.experimental.ExperimentalTypeInference
object Hello {
    konst hello = "hello"
}

@OptIn(ExperimentalTypeInference::class)
fun <E> buildList0(builder: MutableList<E>.() -> Unit): List<E> = mutableListOf<E>().apply { builder() }

konst numbers = buildList0 {
    add(Hello.let { it::hello }.get())
}

fun box(): String {
    numbers
    return "OK"
}
